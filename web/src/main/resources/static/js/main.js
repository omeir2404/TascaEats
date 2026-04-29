document.addEventListener("DOMContentLoaded", () => {
    console.log("TascaEats Web App carregada com sucesso!");

    // Aqui poderás adicionar scripts para validação de formulários,
    // ou chamadas fetch() para interatividade sem recarregar a página.
    // Function to handle a form submission dynamically
    async function fazerLogin(username, password) {
    // Note: If calling directly from the browser, ensure your backend has CORS configured!
    const backendUrl = 'http://localhost:8080/api/utilizadores/login';

    try {
        const response = await fetch(backendUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                username: username, 
                password: password 
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log("Login successful!", data);
            // Redirect the user to the dashboard
            window.location.href = "/restaurantes";
        } else {
            alert("Invalid credentials.");
        }
    } catch (error) {
        console.error("Error connecting to backend:", error);
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById('liveSearchInput');
    const dropdown = document.getElementById('searchDropdown');
    
    // Check if we are on the page with the grid layout
    const defaultViews = document.getElementById('defaultViews');
    const liveGridContainer = document.getElementById('liveGridContainer');
    const liveGridResults = document.getElementById('liveGridResults');

    if (searchInput) {
        searchInput.addEventListener('input', async function(e) {
            const query = e.target.value.trim();

            // 1. If search is empty, restore default views
            if (query.length === 0) {
                if (dropdown) dropdown.style.display = 'none';
                if (liveGridContainer) {
                    liveGridContainer.style.display = 'none';
                    defaultViews.style.display = 'block';
                }
                return;
            }

            try {
                // Fetch real-time data from proxy
                const response = await fetch(`/api/web/live-search?nome=${encodeURIComponent(query)}`);
                
                if (response.ok) {
                    const restaurantes = await response.json();
                    
                    // 2A. We are on the Restaurants page (Update Grid Cards)
                    if (liveGridContainer) {
                        defaultViews.style.display = 'none'; // Hide recents/featured
                        liveGridResults.innerHTML = '';      // Clear old search

                        if (restaurantes.length === 0) {
                            liveGridResults.innerHTML = '<p>No restaurants match your search.</p>';
                        } else {
                            restaurantes.forEach(rest => {
                                // Build a full HTML card
                                const card = document.createElement('div');
                                card.className = 'card';
                                card.innerHTML = `
                                    <h3>${rest.nome}</h3>
                                    <p><strong>Cuisine:</strong> <span>${rest.tipoCozinha || 'N/A'}</span></p>
                                    <p><strong>Status:</strong> <span>${rest.estado || 'Unknown'}</span></p>
                                    <br>
                                    <a href="/restaurantes/${rest.id}/produtos">
                                        <button>View Menu</button>
                                    </a>
                                `;
                                liveGridResults.appendChild(card);
                            });
                        }
                        liveGridContainer.style.display = 'block'; // Show the grid
                        if (dropdown) dropdown.style.display = 'none'; // Ensure dropdown stays hidden
                    } 
                    
                    // 2B. We are on another page (Update Google-style Dropdown)
                    else if (dropdown) {
                        dropdown.innerHTML = '';

                        if (restaurantes.length === 0) {
                            dropdown.innerHTML = '<div style="padding: 10px; color: #888;">No restaurants found.</div>';
                        } else {
                            restaurantes.forEach(rest => {
                                const item = document.createElement('a');
                                item.href = `/restaurantes/${rest.id}/produtos`;
                                item.style.display = 'block';
                                item.style.padding = '10px';
                                item.style.borderBottom = '1px solid #eee';
                                item.style.textDecoration = 'none';
                                item.style.color = '#333';
                                
                                item.addEventListener('mouseenter', () => item.style.backgroundColor = '#f9f9f9');
                                item.addEventListener('mouseleave', () => item.style.backgroundColor = 'white');

                                item.innerHTML = `
                                    <strong>${rest.nome}</strong> 
                                    <span style="font-size: 12px; color: #666; float: right;">${rest.tipoCozinha || ''}</span>
                                `;
                                dropdown.appendChild(item);
                            });
                        }
                        dropdown.style.display = 'block';
                    }
                }
            } catch (error) {
                console.error("Live search failed:", error);
            }
        });

        // Hide dropdown if clicked outside (only relevant for Scenario 2B)
        if (dropdown) {
            document.addEventListener('click', function(event) {
                if (!searchInput.contains(event.target) && !dropdown.contains(event.target)) {
                    dropdown.style.display = 'none';
                }
            });
        }
    }
});
});