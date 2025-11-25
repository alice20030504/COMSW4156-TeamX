# app_mealplan.py
# 7-Day Meal Plan Generator using Speech Input and POE API
# Records user speech, transcribes with local Whisper model, generates meal plan with POE API
# -*- coding: utf-8 -*-

import os
import sys
import asyncio
import gradio as gr
import whisper
import fastapi_poe as fp
import requests

# Add ffmpeg path manually
os.environ["PATH"] += os.pathsep + r"C:\ffmpeg\bin"


# Set UTF-8 encoding for Windows console
if sys.platform == 'win32':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except:
        pass

# ===== Configuration =====
# POE API key - Get from https://poe.com/api_key
POE_API_KEY = 'PFEs5eSCvcdWw4ob2h5B1qbAFlcIJusgTmrgoseGdQM'

# Load Whisper model (load once at startup)
print("Loading Whisper model...")
WHISPER_MODEL = whisper.load_model("tiny.en")
print("Whisper model loaded successfully!")

# ===== Core Functions =====

def fetch_user_info(user_id):
    """
    Fetch user physical data from Spring Boot backend
    
    Args:
        user_id: User ID to fetch data for
    
    Returns:
        Dict with user info or None if request fails
    """
    if not user_id or user_id <= 0:
        return None
    
    try:
        url = f"http://localhost:8080/api/users/{int(user_id)}"
        print(f"Fetching user info from: {url}")
        
        response = requests.get(url, timeout=5)
        
        if response.status_code == 200:
            user_data = response.json()
            print(f"User data retrieved: {user_data}")
            return user_data
        else:
            print(f"Failed to fetch user info: HTTP {response.status_code}")
            return None
    
    except requests.exceptions.RequestException as e:
        print(f"Error fetching user info: {str(e)}")
        return None
    except Exception as e:
        print(f"Unexpected error: {str(e)}")
        return None

def transcribe_audio(audio_path):
    """
    Safe: Copy Gradio temp audio file before Whisper reads it
    """
    if not audio_path:
        return None, "No audio detected"

    # 1. Normalize path (gradio returns string)
    if isinstance(audio_path, str):
        original_path = audio_path.strip('"\'')
        original_path = os.path.normpath(original_path)
    else:
        # fallback
        original_path = str(audio_path)

    # 2. Check existence
    if not os.path.exists(original_path):
        return None, f"Audio file not found: {original_path}"

    # 3. Copy to a safe directory before Whisper reads it
    safe_dir = "audio_cache"
    os.makedirs(safe_dir, exist_ok=True)

    # Use unique filename to avoid conflicts
    import time
    import uuid
    unique_id = f"{int(time.time())}_{uuid.uuid4().hex[:8]}"
    file_ext = os.path.splitext(original_path)[1] or ".wav"
    safe_path = os.path.join(safe_dir, f"recording_{unique_id}{file_ext}")

    import shutil
    shutil.copy(original_path, safe_path)

    print(f"[INFO] Copied audio to safe path: {safe_path}")

    # 4. Transcribe
    try:
        result = WHISPER_MODEL.transcribe(safe_path)
        text = result.get("text", "").strip()
        if not text:
            return None, "No speech detected"
        return text, None

    except Exception as e:
        return None, f"Transcription error: {str(e)}"


async def get_meal_plan_from_poe(combined_input):
    """
    Generate a 7-day meal plan using POE API based on combined user data and speech
    
    Args:
        combined_input: Combined text with user body data and speech transcription
    
    Returns:
        Meal plan text or error message
    """
    if not combined_input or not combined_input.strip():
        return "No input available. Please provide user ID and record your speech."
    
    if not POE_API_KEY:
        return "Error: POE API key not set."
    
    try:
        # Construct the prompt
        final_prompt = f"""
The following information describes the user. Use BOTH the physical data and speech content to generate a personalized 7-day meal plan.

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
- Use the user’s dietary goal/preferences extracted from their speech
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

Now generate **YOUR OWN 7-DAY MEAL PLAN** following this sample structure EXACTLY, reflecting the user goal.
"""
        
        print(f"Sending prompt to POE API...")
        print(f"Prompt: {final_prompt[:200]}...")
        
        # Call POE API
        message = fp.ProtocolMessage(role="user", content=final_prompt)
        full_response = ""
        
        async for partial in fp.get_bot_response(
            messages=[message],
            bot_name='GPT-4o-Mini',
            api_key=POE_API_KEY
        ):
            full_response += partial.text
        
        meal_plan = full_response.strip()
        print(f"Received meal plan ({len(meal_plan)} characters)")
        
        return meal_plan
    
    except Exception as e:
        error_msg = f"POE API error: {str(e)}"
        print(error_msg)
        return error_msg

def generate_meal_plan(combined_input):
    """
    Wrapper function to run async POE API call
    
    Args:
        combined_input: Combined text with user body data and speech transcription
    
    Returns:
        Meal plan text or error message
    """
    return asyncio.run(get_meal_plan_from_poe(combined_input))

def process_speech_to_mealplan(user_id, audio_path):
    """
    Complete workflow: User ID → Fetch Data → Speech → Transcription → Combined Input → Meal Plan
    
    Args:
        user_id: User ID to fetch physical data
        audio_path: Path to recorded audio file
    
    Returns:
        Tuple of (transcription, meal_plan)
    """
    # Step 1: Transcribe audio
    transcript, error = transcribe_audio(audio_path)
    
    if error:
        return error, ""
    
    if not transcript:
        return "No speech detected in the audio.", ""
    
    # Step 2: Fetch user info from backend
    user_info = fetch_user_info(user_id)
    
    # Step 3: Construct combined input
    if user_info:
        age = user_info.get("age", "N/A")
        height = user_info.get("height", "N/A")
        weight = user_info.get("weight", "N/A")
        gender = user_info.get("gender", "N/A")
        activity_level = user_info.get("activityLevel", "N/A")
        
        combined_input = f"""
User Body Data:
Age: {age}, Height: {height} cm, Weight: {weight} kg,
Gender: {gender}, Activity Level: {activity_level}

User Speech:
"{transcript}"
"""
    else:
        combined_input = f"""
User Body Data:
Body data unavailable.

User Speech:
"{transcript}"
"""
    
    # Step 4: Generate meal plan with combined input
    meal_plan = generate_meal_plan(combined_input)
    
    return transcript, meal_plan

# ===== Gradio UI =====

with gr.Blocks(title="7-Day Meal Plan Generator") as demo:
    gr.Markdown("""
    # 🍽️ 7-Day Meal Plan Generator
    
    Record your speech to get a personalized 7-day meal plan generated by POE API (GPT-4o-Mini)!
    
    **How it works:**
    1. Enter your user ID to fetch your physical data (age, height, weight, gender, activity level)
    2. Click the microphone to record your speech
    3. Tell us about your dietary preferences, restrictions, or goals
    4. We'll combine your physical data with your speech to generate a personalized 7-day meal plan
    5. The meal plan includes breakfast, lunch, and dinner for each day
    """)
    
    # API Key status
    with gr.Row():
        poe_status = "✅ Set" if POE_API_KEY else "❌ Not set"
        gr.Markdown(f"**POE API Key:** {poe_status}")
    
    gr.Markdown("---")
    
    # Main interface
    with gr.Row():
        with gr.Column():
            gr.Markdown("### 👤 User Information")
            user_id_input = gr.Number(
                label="Enter your user ID",
                value=None,
                precision=0,
                info="Enter your user ID to fetch your physical data from the backend"
            )
            
            gr.Markdown("### 🎤 Record Your Speech")
            audio_input = gr.Audio(
                sources=["microphone"],
                type="filepath",
                label="Click to Record",
                show_label=True
            )
            process_btn = gr.Button("🚀 Generate Meal Plan", variant="primary", size="lg")
    
    gr.Markdown("---")
    
    # Output displays
    with gr.Row():
        with gr.Column():
            gr.Markdown("### 📝 Transcription")
            transcription_output = gr.Textbox(
                label="What you said:",
                lines=3,
                interactive=False
            )
    
    with gr.Row():
        with gr.Column():
            gr.Markdown("### 🍽️ Your 7-Day Meal Plan")
            mealplan_output = gr.Textbox(
                label="Meal Plan:",
                lines=20,
                interactive=False
            )
    
    # Event handler
    process_btn.click(
        fn=process_speech_to_mealplan,
        inputs=[user_id_input, audio_input],
        outputs=[transcription_output, mealplan_output]
    )
    
    # Instructions
    gr.Markdown("""
    ---
    ### ⚙️ Setup Instructions
    
    1. **POE API Key:**
       - Already set in the code (line 12)
       - Get your own key from: https://poe.com/api_key
       - Each user gets 3000 free credits daily
    
    2. **Backend Service:**
       - Ensure your Spring Boot backend is running on http://localhost:8080
       - The app will fetch user data from: GET /api/users/{id}
    
    3. **Install Dependencies:**
       ```bash
       pip install openai-whisper gradio fastapi-poe requests
       ```
       Note: The Whisper model will be downloaded automatically on first run (tiny.en model, ~75MB)
    
    4. **Run the App:**
       ```bash
       python app_mealplan.py
       ```
       The Whisper model will be loaded at startup (may take a few seconds)
    
    5. **Start Using:**
       - Enter your user ID (to fetch your physical data from the backend)
       - Click the microphone to record
       - Speak your dietary preferences (e.g., "I'm vegetarian and want healthy meals")
       - Click "Generate Meal Plan"
       - View your personalized 7-day meal plan based on both your data and preferences!
    
    ### 🎯 Example Phrases
    
    - "I'm vegetarian and want healthy, easy-to-cook meals"
    - "I need a low-carb meal plan for weight loss"
    - "I want Mediterranean-style meals with lots of vegetables"
    - "I'm allergic to nuts and dairy, and I prefer quick meals"
    - "I want a balanced meal plan with protein-rich foods"
    """)

# ===== Main =====

if __name__ == "__main__":
    print("="*70)
    print("     7-Day Meal Plan Generator")
    print("     Speech → Local Whisper → POE API → Meal Plan")
    print("="*70)
    
    if POE_API_KEY:
        print(f"\n[OK] POE API key is set")
    else:
        print("\n[WARNING] POE API key not set!")
        print("   Please edit this file and set POE_API_KEY.")
        print("   Get your API key from: https://poe.com/api_key")
    
    print("\n[OK] Using local Whisper model (tiny.en) for transcription")
    print("   No OpenAI API key required!")
    
    print("\nStarting Gradio interface...")
    print("="*70)
    
    demo.launch(
        share=False,
        server_name="127.0.0.1",
        server_port=7860
    )

