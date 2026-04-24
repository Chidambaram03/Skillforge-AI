document.addEventListener('DOMContentLoaded', () => {
    const userId = api.getUserId();
    if (!userId) return;

    // DOM Elements
    const quizLoading = document.getElementById('quizLoading');
    const quizContent = document.getElementById('quizContent');
    const questionText = document.getElementById('questionText');
    const optionsContainer = document.getElementById('optionsContainer');
    const submitBtn = document.getElementById('submitBtn');
    const topicBadge = document.getElementById('topicBadge');
    const levelBadge = document.getElementById('levelBadge');
    
    // Timer DOM
    const timeDisplay = document.getElementById('timeDisplay');
    
    // Modal DOM
    const feedbackModal = document.getElementById('feedbackModal');
    const nextQuestionBtn = document.getElementById('nextQuestionBtn');
    const endSessionBtn = document.getElementById('endSessionBtn');

    // State
    let currentQuestion = null;
    let selectedOption = null;
    let startTime = 0;
    let timerInterval = null;
    let secondsElapsed = 0;

    // Timer Logic
    const formatTime = (totalSeconds) => {
        const m = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
        const s = (totalSeconds % 60).toString().padStart(2, '0');
        return `${m}:${s}`;
    };

    const startTimer = () => {
        startTime = Date.now();
        secondsElapsed = 0;
        timeDisplay.textContent = '00:00';
        timerInterval = setInterval(() => {
            secondsElapsed++;
            timeDisplay.textContent = formatTime(secondsElapsed);
        }, 1000);
    };

    const stopTimer = () => {
        clearInterval(timerInterval);
        return Date.now() - startTime; // Total MS
    };

    // Load Question Logic
    const loadNextQuestion = async () => {
        // Reset UI
        quizLoading.classList.remove('hidden');
        quizContent.classList.add('hidden');
        feedbackModal.classList.add('hidden');
        submitBtn.classList.add('hidden');
        optionsContainer.innerHTML = '';
        selectedOption = null;

        try {
            currentQuestion = await api.get(`/quiz/next/${userId}`);
            
            // Populate UI
            topicBadge.textContent = currentQuestion.topic;
            levelBadge.textContent = currentQuestion.difficultyLevel;
            questionText.textContent = currentQuestion.questionText;

            // Expose globally for chatbot context
            window.currentQuestion = {
                topic: currentQuestion.topic,
                level: currentQuestion.difficultyLevel,
                text: currentQuestion.questionText,
                options: {
                    A: currentQuestion.optionA,
                    B: currentQuestion.optionB,
                    C: currentQuestion.optionC,
                    D: currentQuestion.optionD
                }
            };

            // Render Options
            const options = [
                { id: 'A', text: currentQuestion.optionA },
                { id: 'B', text: currentQuestion.optionB },
                { id: 'C', text: currentQuestion.optionC },
                { id: 'D', text: currentQuestion.optionD }
            ];

            options.forEach(opt => {
                const btn = document.createElement('button');
                btn.className = 'option-btn';
                btn.innerHTML = `<strong>${opt.id}.</strong> ${opt.text}`;
                
                btn.addEventListener('click', () => {
                    // Visually Deselect others
                    document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
                    btn.classList.add('selected');
                    selectedOption = opt.id;
                    submitBtn.classList.remove('hidden');
                });
                
                optionsContainer.appendChild(btn);
            });

            // Switch view and Start Timer
            quizLoading.classList.add('hidden');
            quizContent.classList.remove('hidden');
            startTimer();

        } catch (err) {
            console.error('Failed to load question:', err);
            quizLoading.innerHTML = `<h2>Error: ${err.message}</h2><br><a href="dashboard.html" class="btn-primary">Return Home</a>`;
        }
    };

    // Submit Action
    submitBtn.addEventListener('click', async () => {
        if (!selectedOption) return;

        const timeTakenMs = stopTimer();
        submitBtn.disabled = true;
        submitBtn.textContent = 'Analyzing...';

        try {
            const data = {
                questionId: currentQuestion.id,
                selectedOption: selectedOption,
                responseTimeMs: timeTakenMs
            };

            const result = await api.post(`/quiz/submit/${userId}`, data);
            
            // Show Feedback Modal
            showFeedback(result, selectedOption);
            
        } catch (err) {
            console.error('Submit Error', err);
            alert('Error submitting answer.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Answer';
            submitBtn.classList.add('hidden');
        }
    });

    const showFeedback = (result, userChoice) => {
        // Highlight correct/incorrect options visually behind modal
        document.querySelectorAll('.option-btn').forEach(btn => {
            const isCorrect = btn.innerHTML.includes(`<strong>${result.correctOption}.</strong>`);
            const isSelected = btn.classList.contains('selected');
            
            if (isCorrect) {
                btn.style.borderColor = 'var(--success)';
                btn.style.backgroundColor = 'rgba(40,167,69,0.1)';
            } else if (isSelected && !result.isCorrect) {
                btn.style.borderColor = 'var(--danger)';
                btn.style.backgroundColor = 'rgba(220,53,69,0.1)';
            }
        });

        // Set Modal Content
        document.getElementById('feedbackTitle').textContent = result.isCorrect ? 'Correct! 🎉' : 'Incorrect 💡';
        document.getElementById('feedbackTitle').style.color = result.isCorrect ? 'var(--success)' : 'var(--danger)';
        document.getElementById('feedbackMessage').textContent = result.feedback;
        document.getElementById('feedbackExplanation').textContent = result.explanation || 'No detailed explanation available.';
        
        const levelMsg = result.previousLevel === result.newLevel 
            ? `Maintained ${result.newLevel}` 
            : `${result.previousLevel} ➔ ${result.newLevel}`;
        
        document.getElementById('feedbackLevel').textContent = levelMsg;

        // Show Modal
        feedbackModal.classList.remove('hidden');
    };

    // Modal Actions
    nextQuestionBtn.addEventListener('click', loadNextQuestion);
    endSessionBtn.addEventListener('click', () => window.location.href = 'dashboard.html');

    // Initial Start
    loadNextQuestion();
});
