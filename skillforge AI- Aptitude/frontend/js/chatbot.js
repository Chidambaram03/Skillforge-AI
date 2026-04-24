/**
 * SkillForge AI Chatbot — Dynamic Context-Aware Tutor
 * Sends userId + question context for personalised, history-aware responses.
 */
(function () {
    const style = document.createElement('style');
    style.innerHTML = `
        .sf-chatbot {
            position: fixed;
            bottom: 30px;
            right: 30px;
            z-index: 9999;
            font-family: 'Inter', system-ui, sans-serif;
        }

        .sf-chat-toggle {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: linear-gradient(135deg, #2563eb, #1e40af);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: 0 8px 20px rgba(37,99,235,0.35);
            transition: all 0.3s ease;
        }
        .sf-chat-toggle:hover { transform: scale(1.08) translateY(-2px); }

        .sf-chat-window {
            position: absolute;
            bottom: 70px;
            right: 0;
            width: 390px;
            height: 560px;
            background: #fff;
            border-radius: 20px;
            box-shadow: 0 20px 50px rgba(0,0,0,0.15);
            display: none;
            flex-direction: column;
            overflow: hidden;
            animation: sf-pop 0.35s cubic-bezier(0.4,0,0.2,1);
        }
        @keyframes sf-pop {
            from { opacity:0; transform: scale(0.92) translateY(16px); }
            to   { opacity:1; transform: scale(1) translateY(0); }
        }
        .sf-chat-window.open { display: flex; }

        .sf-chat-header {
            padding: 18px 20px;
            background: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .sf-chat-header h4 { margin:0; font-size:1rem; font-weight:700; color:#1e293b; }
        .sf-chat-header p { margin:0; font-size:0.75rem; color:#64748b; }
        .sf-close-btn { cursor:pointer; font-size:22px; color:#94a3b8; line-height:1; }
        .sf-close-btn:hover { color:#1e293b; }

        .sf-chat-messages {
            flex:1;
            padding: 16px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
            background: #f8fafc;
        }

        .sf-bubble {
            max-width: 85%;
            padding: 11px 15px;
            border-radius: 18px;
            font-size: 0.9rem;
            line-height: 1.55;
            white-space: pre-wrap;
        }
        .sf-bubble-user {
            align-self: flex-end;
            background: #2563eb;
            color: #fff;
            border-bottom-right-radius: 4px;
        }
        .sf-bubble-ai {
            align-self: flex-start;
            background: #fff;
            color: #334155;
            border-bottom-left-radius: 4px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }

        /* Typing animation */
        .sf-typing { display: flex; gap: 4px; padding: 14px 16px; }
        .sf-typing span {
            width: 7px; height: 7px;
            background: #94a3b8;
            border-radius: 50%;
            animation: sf-bounce 1.2s infinite;
        }
        .sf-typing span:nth-child(2) { animation-delay: 0.2s; }
        .sf-typing span:nth-child(3) { animation-delay: 0.4s; }
        @keyframes sf-bounce {
            0%,60%,100% { transform: translateY(0); }
            30% { transform: translateY(-8px); }
        }

        .sf-chat-input-row {
            padding: 14px 16px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            gap: 10px;
            background: #fff;
        }
        .sf-chat-input-row input {
            flex:1;
            border: 1.5px solid #e2e8f0;
            border-radius: 12px;
            padding: 10px 14px;
            font-size: 0.9rem;
            outline: none;
            transition: border-color 0.2s;
        }
        .sf-chat-input-row input:focus { border-color: #2563eb; }
        .sf-send-btn {
            background: #2563eb;
            color: #fff;
            border: none;
            padding: 10px 18px;
            border-radius: 12px;
            font-weight: 600;
            cursor: pointer;
            font-size: 0.9rem;
            transition: background 0.2s;
        }
        .sf-send-btn:hover { background: #1d4ed8; }
        .sf-send-btn:disabled { background: #93c5fd; cursor: not-allowed; }

        .sf-chat-suggestions {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            padding: 0 16px 12px;
            background: #fff;
        }
        .sf-chip {
            font-size: 0.75rem;
            background: #eff6ff;
            color: #2563eb;
            border: 1px solid #bfdbfe;
            border-radius: 20px;
            padding: 4px 10px;
            cursor: pointer;
            transition: all 0.15s;
        }
        .sf-chip:hover { background: #2563eb; color: #fff; }
    `;
    document.head.appendChild(style);

    // Build DOM
    const container = document.createElement('div');
    container.className = 'sf-chatbot';
    container.innerHTML = `
        <div class="sf-chat-window" id="sfChatWindow">
            <div class="sf-chat-header">
                <div>
                    <h4>SkillForge Assistant</h4>
                    <p>Your personalised aptitude tutor</p>
                </div>
                <span class="sf-close-btn" id="sfCloseBtn">&times;</span>
            </div>
            <div class="sf-chat-messages" id="sfChatMessages">
                <div class="sf-bubble sf-bubble-ai">👋 Hello! I'm your elite aptitude tutor.\n\nTry asking:\n• <b>explain</b> — get a breakdown of the current question\n• <b>hint</b> — get a clue without the answer\n• <b>my weak topics</b> — see where to improve\n• <b>shortcuts</b> — speed-solving strategies</div>
            </div>
            <div class="sf-chat-suggestions" id="sfChips">
                <span class="sf-chip" data-msg="explain the current question">Explain this ❓</span>
                <span class="sf-chip" data-msg="give me a hint">Hint 💡</span>
                <span class="sf-chip" data-msg="show my weak topics">My weaknesses 📊</span>
                <span class="sf-chip" data-msg="give me solving shortcuts">Shortcuts ⚡</span>
            </div>
            <form class="sf-chat-input-row" id="sfChatForm">
                <input type="text" id="sfChatInput" placeholder="Ask your tutor anything..." autocomplete="off">
                <button class="sf-send-btn" type="submit" id="sfSendBtn">Send</button>
            </form>
        </div>
        <div class="sf-chat-toggle" id="sfChatToggle">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
        </div>
    `;
    document.body.appendChild(container);

    // Elements
    const toggle   = document.getElementById('sfChatToggle');
    const chatWin  = document.getElementById('sfChatWindow');
    const closeBtn = document.getElementById('sfCloseBtn');
    const msgArea  = document.getElementById('sfChatMessages');
    const form     = document.getElementById('sfChatForm');
    const input    = document.getElementById('sfChatInput');
    const sendBtn  = document.getElementById('sfSendBtn');
    const chips    = document.querySelectorAll('.sf-chip');

    // Toggle
    toggle.addEventListener('click', () => {
        chatWin.classList.toggle('open');
        if (chatWin.classList.contains('open')) input.focus();
    });
    closeBtn.addEventListener('click', () => chatWin.classList.remove('open'));

    // Quick chips
    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            input.value = chip.dataset.msg;
            input.focus();
        });
    });

    // Send
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;

        addBubble('user', text);
        input.value = '';
        sendBtn.disabled = true;

        const typingEl = addTyping();

        try {
            const userId = localStorage.getItem('userId') || '0';
            const context = window.currentQuestion || null;

            // Use the central API URL if available, fallback to localhost:8080
            const baseUrl = (typeof API_BASE_URL !== 'undefined') ? API_BASE_URL : 'http://localhost:8080/api';
            const apiUrl = `${baseUrl}/chat/ask`;

            const res = await fetch(apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text, userId, context })
            });
            
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.reply || `HTTP error! status: ${res.status}`);
            }

            const data = await res.json();
            typingEl.remove();
            addBubble('ai', data.reply || "I didn't quite catch that. Could you rephrase?");
        } catch (err) {
            console.error('Chatbot Error:', err);
            typingEl.remove();
            
            let errorMsg = "I'm having trouble connecting to my brain (the server).";
            if (err.message.includes('Failed to fetch') || err.message.includes('NetworkError')) {
                errorMsg += " It looks like the backend isn't running on port 8080. Please ensure the server is started.";
            } else {
                errorMsg += ` Detail: ${err.message}`;
            }
            addBubble('ai', errorMsg);
        } finally {
            sendBtn.disabled = false;
        }
    });

    function addBubble(role, text) {
        const div = document.createElement('div');
        div.className = `sf-bubble sf-bubble-${role}`;
        div.innerHTML = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>');
        msgArea.appendChild(div);
        msgArea.scrollTop = msgArea.scrollHeight;
        return div;
    }

    function addTyping() {
        const div = document.createElement('div');
        div.className = 'sf-bubble sf-bubble-ai sf-typing';
        div.innerHTML = '<span></span><span></span><span></span>';
        msgArea.appendChild(div);
        msgArea.scrollTop = msgArea.scrollHeight;
        return div;
    }
})();
