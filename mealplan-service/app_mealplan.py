# app_mealplan.py (Microservice Version)
# Uses fastapi-poe library for POE API integration
# -*- coding: utf-8 -*-

import os
import asyncio
import fastapi_poe as fp

# ===== Configuration =====
# POE_API_KEY = os.getenv("POE_API_KEY")
POE_API_KEY = 'PFEs5eSCvcdWw4ob2h5B1qbAFlcIJusgTmrgoseGdQM'

async def get_meal_plan_from_poe(combined_input: str):
    """
    Generate a 7-day meal plan using POE API via direct HTTP requests.
    This avoids fastapi-poe version compatibility issues.
    """

    # Validate input
    if not combined_input or not combined_input.strip():
        return "No input available. Please provide user body data (id, age, height, weight, gender, goal)."

    if not POE_API_KEY:
        return "Error: POE API key not set."

    # Construct prompt
    final_prompt = f"""
The following information describes the user. Use the physical data and instruction to generate a personalized 7-day meal plan.

{combined_input}

You are a diet-planning assistant. Generate a 7-day meal plan in exactly the same format and style as the sample provided below — no markdown (#, *, -), no explanations, no introductions.

IMPORTANT RULES:
- Do NOT output markdown symbols (#, *, -, etc.)
- Do NOT add commentary or disclaimers
- Do NOT explain anything before or after the plan
- ONLY output the meal plan in the exact required structure
- Include kcal values for each ingredient
- Include hydration section each day
- Include a daily total calculation line formatted as: → Daily Total = X kcal
- Base the meal plan on the user's fitness goal (CUT for weight loss, BULK for weight gain)
- Consider the user's age, height, weight, and gender when calculating appropriate calorie targets
- Output 7 full days

=============== SAMPLE FORMAT (FOLLOW EXACTLY) ===============

DAY 1 — Total ≈ 1198 kcal
Breakfast — 268 kcal

1 whole egg (55 g) — 72 kcal
2 egg whites (50 g) — 26 kcal
Spinach (100 g) — 23 kcal
Tomato (120 g) — 22 kcal
Olive oil for sauté (3 ml) — 25 kcal
Method: Boiled egg + sautéed spinach & tomato.

Lunch — 422 kcal

Chicken breast (120 g) — 198 kcal
Broccoli (150 g) — 51 kcal
Quinoa, dry (40 g → ~120 g cooked) — 173 kcal
Method: Air-fried/grilled chicken, steamed broccoli, boiled quinoa.

Dinner — 331 kcal

Tofu (150 g, firm) — 117 kcal
Lettuce (120 g) — 15 kcal
Mushrooms (80 g) — 22 kcal
Olive oil (2 ml) — 18 kcal
Method: Tofu & mushroom clear soup + lettuce salad.

Hydration

Water 2 L
Green tea — 0 kcal
Vegetable broth — 50 kcal
→ Daily Total = 1198 kcal

==============================================================

Now generate YOUR OWN 7-DAY MEAL PLAN following this sample structure EXACTLY.
"""

    print("[mealplan] Sending prompt to POE API...")
    print(f"[mealplan] API Key present: {bool(POE_API_KEY)}")
    print(f"[mealplan] Prompt length: {len(final_prompt)} characters")

    try:
        # Use fastapi-poe library (same as original app_mealplan.py)
        # Create ProtocolMessage for the prompt
        message = fp.ProtocolMessage(role="user", content=final_prompt)
        full_response = ""
        
        # Call POE API using get_bot_response (streaming response)
        # Using the same method as the original working file
        async for partial in fp.get_bot_response(
            messages=[message],
            bot_name='GPT-4o-Mini',
            api_key=POE_API_KEY
        ):
            full_response += partial.text
        
        meal_plan = full_response.strip()
        print(f"[mealplan] Received meal plan ({len(meal_plan)} characters)")
        
        if not meal_plan:
            return "POE API returned empty response."
        
        return meal_plan

    except Exception as e:
        error_msg = f"POE API error: {str(e)}"
        print(f"[mealplan] {error_msg}")
        import traceback
        print(f"[mealplan] Traceback: {traceback.format_exc()}")
        return error_msg


def generate_meal_plan(combined_input):
    """
    Synchronous wrapper to call async POE API function.
    """
    return asyncio.run(get_meal_plan_from_poe(combined_input))
