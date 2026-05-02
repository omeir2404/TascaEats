document.addEventListener("DOMContentLoaded", () => {
    console.log("TascaEats Web App carregada com sucesso!");

    // --- Live Search Function ---
    const searchInput = document.getElementById('liveSearchInput');
    const dropdown = document.getElementById('searchDropdown');
    
    const defaultViews = document.getElementById('defaultViews');
    const liveGridContainer = document.getElementById('liveGridContainer');
    const liveGridResults = document.getElementById('liveGridResults');

    if (searchInput) {
        searchInput.addEventListener('input', async function(e) {
            const query = e.target.value.trim();

            if (query.length === 0) {
                if (dropdown) dropdown.style.display = 'none';
                if (liveGridContainer) {
                    liveGridContainer.style.display = 'none';
                    defaultViews.style.display = 'block';
                }
                return;
            }

            try {
                const response = await fetch(`/api/web/live-search?nome=${encodeURIComponent(query)}`);
                
                if (response.ok) {
                    const restaurantes = await response.json();
                    
                    if (liveGridContainer) {
                        defaultViews.style.display = 'none'; 
                        liveGridResults.innerHTML = '';      

                        if (restaurantes.length === 0) {
                            liveGridResults.innerHTML = '<div class="empty-state"><p>Não existem restaurantes para esta pesquisa.</p></div>';
                        } else {
                            restaurantes.forEach(rest => {
                                const card = document.createElement('div');
                                card.className = 'card';
                                const tipo = rest.tipoRestaurante || rest.tipoCozinha || 'OUTRO';
                                const estado = rest.estado || 'Unknown';
                                const statusClass = estado === 'ABERTO' ? 'status-pill status-pill--open' : 'status-pill status-pill--closed';
                                card.innerHTML = `
                                    <div class="pill-row">
                                        <span class="pill">${tipo}</span>
                                        <span class="${statusClass}">${estado}</span>
                                    </div>
                                    <div>
                                        <h3>${rest.nome}</h3>
                                        <p>Entrada direta no menu do restaurante.</p>
                                    </div>
                                    <div class="card__actions">
                                        <a href="/restaurantes/${rest.id}/produtos">
                                            <button>Ver menu</button>
                                        </a>
                                    </div>
                                `;
                                liveGridResults.appendChild(card);
                            });
                        }
                        liveGridContainer.style.display = 'block'; 
                        if (dropdown) dropdown.style.display = 'none'; 
                    } 
                    else if (dropdown) {
                        dropdown.innerHTML = '';
                        if (restaurantes.length === 0) {
                            dropdown.innerHTML = '<div style="padding: 12px; color: #667085;">Nenhum restaurante encontrado.</div>';
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
                                    <span style="font-size: 12px; color: #666; float: right;">${rest.tipoRestaurante || rest.tipoCozinha || ''}</span>
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

        if (dropdown) {
            document.addEventListener('click', function(event) {
                if (!searchInput.contains(event.target) && !dropdown.contains(event.target)) {
                    dropdown.style.display = 'none';
                }
            });
        }
    }
});
