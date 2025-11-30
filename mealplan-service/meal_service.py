# meal_service.py
# FastAPI microservice for meal plan generation
# -*- coding: utf-8 -*-

from fastapi import FastAPI, HTTPException
from fastapi.responses import RedirectResponse
from pydantic import BaseModel
from app_mealplan import generate_meal_plan

app = FastAPI(
    title="Meal Plan Service",
    version="1.0.0"
)

# =======================
# Request / Response Models
# =======================

class MealPlanRequest(BaseModel):
    id: int
    age: int
    height: float   # cm
    weight: float   # kg
    gender: str     # "MALE" / "FEMALE"
    goal: str       # "CUT" / "BULK"


class MealPlanResponse(BaseModel):
    meal_plan: str


# =======================
# Health Check
# =======================
@app.get("/")
def root():
    return RedirectResponse(url="/docs")
@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "mealplan-service"
    }


# =======================
# Meal Plan Generation
# =======================

@app.post("/mealplan", response_model=MealPlanResponse)
def generate_mealplan(request: MealPlanRequest):
    """
    Generate a 7-day meal plan based on user body data.
    Delegates GPT generation to app_mealplan.generate_meal_plan().
    """

    try:
        # -------- Construct combined_input text (body data + instruction) --------
        combined_input = (
            f"User Body Data:\n"
            f"Age: {request.age} years\n"
            f"Height: {request.height} cm\n"
            f"Weight: {request.weight} kg\n"
            f"Gender: {request.gender}\n"
            f"Fitness Goal: {request.goal}\n\n"
            f"Instruction:\n"
            f"Generate a personalized 7-day meal plan tailored to this user's body data and fitness goal ({request.goal}). "
            f"The meal plan should support their goal of {request.goal.lower()}ing, considering their age ({request.age} years), "
            f"height ({request.height} cm), weight ({request.weight} kg), and gender ({request.gender})."
        )

        # -------- Call POE-based generator --------
        meal_plan = generate_meal_plan(combined_input)

        # -------- Validate output --------
        if not meal_plan or not meal_plan.strip():
            raise HTTPException(
                status_code=500,
                detail="Meal plan generator returned an empty result."
            )

        return MealPlanResponse(meal_plan=meal_plan)

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error generating meal plan: {str(e)}"
        )

# =======================
# Local Dev Run
# =======================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)
