# Use official Python image
FROM python:3.12-slim

# Install system dependencies for common pip packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential gcc git curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for safety
RUN useradd -ms /bin/bash coder

# Create workspace directory where user code will live
WORKDIR /workspace
RUN chown coder:coder /workspace

# Create a virtual environment for Python packages
RUN python -m venv /opt/venv && \
    chown -R coder:coder /opt/venv

# Use the virtualenv by default
ENV PATH="/opt/venv/bin:$PATH"

# Switch to non-root user
USER coder

# Default command - we'll usually override with docker exec
CMD ["bash"]