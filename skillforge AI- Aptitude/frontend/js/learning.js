document.addEventListener('DOMContentLoaded', () => {
    const topicList = document.getElementById('topicList');
    const noSelection = document.getElementById('noSelection');
    const videoPlayerSection = document.getElementById('videoPlayerSection');
    const currentTopicTitle = document.getElementById('currentTopicTitle');
    const videoIframe = document.getElementById('videoIframe');
    const topicDescription = document.getElementById('topicDescription');

    const API_BASE_URL = 'http://localhost:8080/api';

    // Fetch all videos to populate the sidebar
    fetch(`${API_BASE_URL}/videos`)
        .then(response => response.json())
        .then(videos => {
            topicList.innerHTML = ''; // Clear loading state
            
            if (videos.length === 0) {
                topicList.innerHTML = '<li>No videos available</li>';
                return;
            }

            // Group videos by topic (though currently we expect one per topic)
            const uniqueTopics = [...new Set(videos.map(v => v.topic))];

            uniqueTopics.forEach(topic => {
                const li = document.createElement('li');
                li.textContent = topic;
                li.addEventListener('click', () => {
                    selectTopic(topic, videos.find(v => v.topic === topic));
                    
                    // Update active state in sidebar
                    document.querySelectorAll('#topicList li').forEach(item => item.classList.remove('active'));
                    li.classList.add('active');
                });
                topicList.appendChild(li);
            });
        })
        .catch(err => {
            console.error('Error fetching videos:', err);
            topicList.innerHTML = '<li>Error loading topics</li>';
        });

    function selectTopic(topicName, videoData) {
        noSelection.classList.add('hidden');
        videoPlayerSection.classList.remove('hidden');

        currentTopicTitle.textContent = topicName;
        videoIframe.src = videoData.videoUrl;
        topicDescription.textContent = videoData.description || 'No description available for this topic.';

        // Set chatbot context
        window.currentQuestion = { topic: topicName };
    }

    // Auto-select if topic is in URL (optional enhancement)
    const urlParams = new URLSearchParams(window.location.search);
    const topicParam = urlParams.get('topic');
    if (topicParam) {
        // We'll let the initial fetch handle the selection logic if needed
    }
});
