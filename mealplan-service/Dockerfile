FROM python:3.12-slim

WORKDIR /app

# Accept POE_API_KEY as build argument
ARG POE_API_KEY
ENV POE_API_KEY=${POE_API_KEY}

# Copy requirements first for better caching
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application files
COPY meal_service.py .
COPY app_mealplan.py .

# Expose port 5001
EXPOSE 5001

# Run the FastAPI service
CMD ["uvicorn", "meal_service:app", "--host", "0.0.0.0", "--port", "5001"]

