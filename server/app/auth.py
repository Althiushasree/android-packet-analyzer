import os
from fastapi import Header, HTTPException, status
from dotenv import load_dotenv

load_dotenv()

API_KEY_ENV = os.getenv("API_KEY", "nt04-network-admin-secret-token")


def verify_api_key(x_api_key: str = Header(...)):
    if not x_api_key or x_api_key.strip() != API_KEY_ENV.strip():
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing X-API-Key header"
        )
    return x_api_key
