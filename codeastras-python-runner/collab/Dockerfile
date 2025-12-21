FROM python:3.12-slim

# Avoid buffering issues
ENV PYTHONUNBUFFERED=1

# Create workspace for live files
WORKDIR /workspace

# Install basic tools (optional but useful)
RUN apt-get update && apt-get install -y \
    bash \
    git \
    curl \
 && rm -rf /var/lib/apt/lists/*

# Container stays alive
CMD ["tail", "-f", "/dev/null"]
