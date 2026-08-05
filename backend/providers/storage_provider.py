import os
import uuid
import base64
import httpx
from typing import Tuple
from backend.config import settings
from backend.core.logging import logger

UPLOAD_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "static", "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

class StorageProvider:
    """Abstract interface for storing uploaded assets and generated images without storing heavy binaries in DB."""
    async def fun_upload_file(self, file_bytes: bytes, file_name: str, content_type: str = "image/jpeg") -> str:
        raise NotImplementedError

    async def fun_store_image_reference(self, image_source: str) -> Tuple[str, str]:
        """Takes an image URL or data URI, saves/references it securely, and returns (public_url, storage_key)."""
        raise NotImplementedError

class LocalStorageProvider(StorageProvider):
    async def fun_upload_file(self, file_bytes: bytes, file_name: str, content_type: str = "image/jpeg") -> str:
        ext = ".jpg" if "jpeg" in content_type else (".png" if "png" in content_type else ".bin")
        unique_filename = f"{uuid.uuid4().hex}_{file_name}{ext}"
        filepath = os.path.join(UPLOAD_DIR, unique_filename)
        
        with open(filepath, "wb") as f:
            f.write(file_bytes)
            
        logger.info(f"Saved asset to local storage: {filepath}")
        return f"/static/uploads/{unique_filename}"

    async def fun_store_image_reference(self, image_source: str) -> Tuple[str, str]:
        storage_key = f"img_{uuid.uuid4().hex}"
        
        if image_source.startswith("data:image/"):
            try:
                header, encoded = image_source.split(",", 1)
                data = base64.b64decode(encoded)
                ext = ".jpg" if "jpeg" in header or "jpg" in header else ".png"
                filename = f"{storage_key}{ext}"
                filepath = os.path.join(UPLOAD_DIR, filename)
                with open(filepath, "wb") as f:
                    f.write(data)
                public_url = f"/static/uploads/{filename}"
                return public_url, storage_key
            except Exception as e:
                logger.error(f"Error decoding base64 image data: {e}")
                
        # For remote URLs, store the URL as the public reference and storage key
        return image_source, storage_key

storage_provider = LocalStorageProvider()
