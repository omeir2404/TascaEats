// ##############################################################
//                           TASCAEATS
//                        CSS GRUPO: 25
//        Autores:                 Numero Aluno:
//     - Omeir Haroon                61810
//     - Isa David                   61871
//     - Diogo Roque                 61857
// ##############################################################

document.addEventListener("DOMContentLoaded", () => {
    console.log("TascaEats Web App carregada com sucesso!");

    const headerCart = document.getElementById('headerCart');
    const headerCartTrigger = document.getElementById('headerCartTrigger');
    const headerCartItems = document.getElementById('headerCartItems');
    const headerCartEmpty = document.getElementById('headerCartEmpty');
    const headerCartTotal = document.getElementById('headerCartTotal');

    function readHeaderCart() {
        try {
            const raw = sessionStorage.getItem('tascaeatsCart');
            return raw ? JSON.parse(raw) : [];
        } catch (error) {
            return [];
        }
    }

    function formatHeaderCartPrice(value) {
        const number = Number(value) || 0;
        return `${number.toFixed(2)} €`;
    }

    function renderHeaderCart() {
        if (!headerCartItems || !headerCartEmpty || !headerCartTotal) {
            return;
        }

        const cart = readHeaderCart();
        const total = cart.reduce((sum, item) => sum + (Number(item.preco || 0) * Number(item.quantidade || 0)), 0);

        headerCartItems.innerHTML = '';

        if (cart.length === 0) {
            headerCartEmpty.style.display = 'block';
        } else {
            headerCartEmpty.style.display = 'none';

            cart.forEach(item => {
                const li = document.createElement('li');
                li.className = 'header-cart__item';
                li.innerHTML = `
                    <div class="header-cart__item-top">
                        <span class="header-cart__item-name">${item.nome}</span>
                        <span class="header-cart__item-price">${formatHeaderCartPrice(item.preco)}</span>
                    </div>
                    <span class="header-cart__item-details">${item.quantidade} x ${formatHeaderCartPrice(item.preco)}</span>
                `;
                headerCartItems.appendChild(li);
            });
        }

        headerCartTotal.textContent = formatHeaderCartPrice(total);
    }

    if (headerCart && headerCartTrigger) {
        renderHeaderCart();

        headerCartTrigger.addEventListener('click', function(event) {
            event.stopPropagation();
            renderHeaderCart();
            const isOpen = headerCart.classList.toggle('is-open');
            headerCartTrigger.setAttribute('aria-expanded', String(isOpen));
        });

        document.addEventListener('click', function(event) {
            if (!headerCart.contains(event.target)) {
                headerCart.classList.remove('is-open');
                headerCartTrigger.setAttribute('aria-expanded', 'false');
            }
        });

        window.addEventListener('storage', renderHeaderCart);
    }

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
                                const imageMap = {
                                    1: '/img/tasca.jpg',
                                    2: '/img/pizza.png',
                                    3: '/img/sushi.jpeg',
                                    4: '/img/economico.jpeg'
                                };
                                const imageUrl = imageMap[rest.id] || '';
                                card.innerHTML = `
                                    <div class="card__image" aria-hidden="true" style="${imageUrl ? `background-image: url(${imageUrl}); background-size: cover; background-position: center; background-repeat: no-repeat;` : ''}"></div>
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
