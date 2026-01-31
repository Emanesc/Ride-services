const API_URLS = {
    RIDE: 'http://localhost:8081/api/rides',
    BOOKING: 'http://localhost:8082/api/bookings',
    PAYMENT: 'http://localhost:8083/api/payments',
    EVALUATION: 'http://localhost:8083/api/evaluations',
    AVAILABILITY: 'http://localhost:8082/api/bookings/availability'
};

let currentRideForBooking = null;
let currentBookingForPayment = null;
let currentEvaluationInfo = null;  // Stocke les infos complètes du booking pour l'évaluation
let availableDrivers = [];

// ===== DRIVERS =====
async function loadDrivers() {
    try {
        console.log('[DEBUG] Loading drivers');
        const response = await fetch(`${API_URLS.RIDE}/drivers`);
        
        if (!response.ok) {
            throw new Error('Failed to load drivers');
        }
        
        availableDrivers = await response.json();
        console.log('[DEBUG] Drivers loaded:', availableDrivers);
        populateDriverSelect();
    } catch (error) {
        console.error('[ERROR] loadDrivers:', error);
        showToast('Failed to load drivers', 'error');
    }
}

function populateDriverSelect() {
    const select = document.getElementById('post-driver');
    if (!select) return;
    
    select.innerHTML = '<option value="">Select a driver...</option>';
    
    availableDrivers.forEach(driver => {
        const option = document.createElement('option');
        option.value = driver.id;
        option.textContent = `${driver.name} (Rating: ${driver.rating} - ${driver.totalRides} rides)`;
        option.dataset.driverName = driver.name;
        select.appendChild(option);
    });
}

// ===== NAVIGATION =====
function showSection(sectionId) {
    document.querySelectorAll('main').forEach(el => {
        el.classList.remove('active-section');
        el.classList.add('hidden-section');
    });

    document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));

    const target = document.getElementById(sectionId + '-section');
    target.classList.remove('hidden-section');
    target.classList.add('active-section');

    const btn = Array.from(document.querySelectorAll('.nav-btn')).find(b => {
        if (sectionId === 'rides') return b.innerText.toLowerCase().includes('find');
        if (sectionId === 'post-ride') return b.innerText.toLowerCase().includes('post');
        if (sectionId === 'my-bookings') return b.innerText.toLowerCase().includes('bookings');
        if (sectionId === 'evaluations') return b.innerText.toLowerCase().includes('evaluation');
        return false;
    });
    if (btn) btn.classList.add('active');

    if (sectionId === 'rides') {
        searchRides();
    } else if (sectionId === 'post-ride') {
        if (availableDrivers.length === 0) loadDrivers();
    } else if (sectionId === 'my-bookings') {
        loadAllMyBookings();
    } else if (sectionId === 'evaluations') {
        loadAllBookingsForEvaluation();  // Charger les bookings pour le formulaire
    }
}

// ===== POST RIDE =====
async function postRide() {
    const dep = document.getElementById('post-departure').value;
    const dest = document.getElementById('post-destination').value;
    const time = document.getElementById('post-time').value;
    const seats = document.getElementById('post-seats').value;
    const price = document.getElementById('post-price').value;
    const driverSelect = document.getElementById('post-driver');
    const driverId = driverSelect.value;
    const driverName = driverSelect.selectedOptions[0]?.dataset.driverName;

    if (!dep || !dest || !time || !seats || !price || !driverId) {
        showToast('Please fill in all fields including driver selection', 'error');
        return;
    }

    try {
        // Formater la date correctement pour LocalDateTime
        // Enlever les millisecondes si présentes et s'assurer du format correct
        let departureTime = time;
        
        // Si la date contient déjà les secondes (format: 2026-01-26T22:07:53)
        if (departureTime.length === 19) {
            // Format correct, rien à faire
        } 
        // Si la date ne contient pas les secondes (format: 2026-01-26T22:07)
        else if (departureTime.length === 16) {
            // Ajouter :00 pour les secondes
            departureTime = departureTime + ':00';
        }
        // Sinon, nettoyer le format (enlever tout après les secondes)
        else if (departureTime.length > 19) {
            departureTime = departureTime.substring(0, 19);
        }

        console.log('[DEBUG] Sending ride data:', {
            departure: dep,
            destination: dest,
            departureTime: departureTime,
            availableSeats: parseInt(seats),
            price: parseFloat(price),
            driverId: driverId,
            driverName: driverName
        });

        const response = await fetch(API_URLS.RIDE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                departure: dep,
                destination: dest,
                departureTime: departureTime,
                availableSeats: parseInt(seats),
                price: parseFloat(price),
                driverId: driverId,
                driverName: driverName
            })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to create ride' }));
            console.error('[ERROR] Response:', errorData);
            throw new Error(errorData.error || errorData.message || 'Failed to create ride');
        }

        const result = await response.json();
        console.log('[SUCCESS] Ride created:', result);
        
        showToast('Ride published successfully!', 'success');
        showSection('rides');
        
        // Clear form
        document.getElementById('post-departure').value = '';
        document.getElementById('post-destination').value = '';
        document.getElementById('post-time').value = '';
        document.getElementById('post-seats').value = '3';
        document.getElementById('post-price').value = '25';
        document.getElementById('post-driver').value = '';
    } catch (error) {
        console.error('[ERROR] postRide:', error);
        showToast(error.message, 'error');
    }
}

// ===== RIDES =====
async function searchRides() {
    const dep = document.getElementById('search-departure').value;
    const dest = document.getElementById('search-destination').value;
    const list = document.getElementById('rides-list');

    list.innerHTML = '<div class="loading-state">Finding best rides for you...</div>';

    try {
        let url = API_URLS.RIDE;
        if (dep || dest) {
            url += `/search?`;
            if (dep) url += `departure=${encodeURIComponent(dep)}&`;
            if (dest) url += `destination=${encodeURIComponent(dest)}`;
        }

        console.log('[DEBUG] Fetching rides from:', url);
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error('Failed to fetch rides');
        }

        const rides = await response.json();
        console.log('[DEBUG] Rides received:', rides);
        renderRides(rides);
    } catch (error) {
        console.error('[ERROR] searchRides:', error);
        list.innerHTML = `<div class="loading-state" style="color: var(--error)">Error: ${error.message}</div>`;
    }
}

function renderRides(rides) {
    const list = document.getElementById('rides-list');
    if (rides.length === 0) {
        list.innerHTML = '<div class="loading-state">No rides found. Try different criteria.</div>';
        return;
    }

    list.innerHTML = rides.map(ride => `
        <div class="card">
            <div class="card-header">
                <div class="route">${ride.departure} → ${ride.destination}</div>
                <div class="price">${ride.price} MRU</div>
            </div>
            <div class="card-details">
                <div class="detail-row">
                    <span>Driver</span>
                    <span>${ride.driverName || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span>Departure</span>
                    <span>${new Date(ride.departureTime).toLocaleString()}</span>
                </div>
                <div class="detail-row">
                    <span>Seats</span>
                    <span id="seats-${ride.id}">${ride.availableSeats} available</span>
                </div>
            </div>
            <div class="card-actions">
                <button class="action-btn" onclick="checkAvailabilityAndBook(${ride.id}, '${ride.departure}', '${ride.destination}', ${ride.price})">
                    Check Availability & Book
                </button>
                <button class="delete-btn" onclick="deleteRide(${ride.id})" title="Delete this ride">
                    Delete
                </button>
            </div>
        </div>
    `).join('');
}

// ===== DELETE RIDE =====
async function deleteRide(rideId) {
    if (!confirm('Are you sure you want to delete this ride? This action cannot be undone.')) {
        return;
    }

    try {
        console.log('[DEBUG] Deleting ride:', rideId);
        const response = await fetch(`${API_URLS.RIDE}/${rideId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Failed to delete ride' }));
            throw new Error(error.error || error.message || 'Failed to delete ride');
        }

        console.log('[SUCCESS] Ride deleted:', rideId);
        showToast('Ride deleted successfully', 'success');
        searchRides();
    } catch (error) {
        console.error('[ERROR] deleteRide:', error);
        showToast(error.message, 'error');
    }
}

// ===== AVAILABILITY & BOOKING =====
async function checkAvailabilityAndBook(rideId, dep, dest, ridePrice) {
    showToast('Checking availability...', 'info');
    console.log(`[DEBUG] Checking availability for Ride ID: ${rideId}`);

    try {
        const url = `${API_URLS.AVAILABILITY}/${rideId}`;
        console.log(`[DEBUG] Fetching URL: ${url}`);

        const response = await fetch(url);
        console.log(`[DEBUG] Response received. Status: ${response.status}`);

        if (!response.ok) {
            console.error(`[DEBUG] HTTP Error! Status: ${response.status} ${response.statusText}`);
            const errorData = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(errorData.message || errorData.error || `HTTP Error: ${response.status}`);
        }

        const data = await response.json();
        console.log('[DEBUG] Data received:', data);

        if (data.available) {
            console.log('[DEBUG] Ride available, opening modal.');
            
            const price = data.price !== undefined ? data.price : ridePrice;
            
            currentRideForBooking = { 
                id: rideId, 
                from: dep, 
                to: dest, 
                price: price,
                availableSeats: data.availableSeats
            };

            const detailsHtml = `
                <p><strong>Route:</strong> ${dep} to ${dest}</p>
                <p><strong>Status:</strong> ${data.message || 'Available'}</p>
                <p><strong>Seats Left:</strong> ${data.availableSeats}</p>
                <p><strong>Price per Seat:</strong> ${price} MRU</p>
            `;

            document.getElementById('booking-ride-details').innerHTML = detailsHtml;
            document.getElementById('availability-status').innerText = "";

            document.getElementById('price-per-seat').innerText = `${price} MRU`;
            document.getElementById('booking-seats').value = 1;
            document.getElementById('booking-seats').max = data.availableSeats;
            updateTotal();

            document.getElementById('booking-modal').style.display = 'flex';
        } else {
            console.warn('[DEBUG] Ride not available:', data.message);
            showToast(data.message || 'Ride is fully booked', 'error');
        }
    } catch (error) {
        console.error('[DEBUG] Fetch error:', error);
        showToast('Service unavailable: ' + error.message, 'error');
    }
}

function updateTotal() {
    if (!currentRideForBooking) return;
    const seats = parseInt(document.getElementById('booking-seats').value) || 1;
    const total = seats * currentRideForBooking.price;
    document.getElementById('total-price').innerText = `${total} MRU`;
}

async function confirmBooking() {
    const passengerId = document.getElementById('booking-passenger-id').value;
    const seats = document.getElementById('booking-seats').value;

    if (!passengerId) {
        showToast('Please enter a Passenger ID', 'error');
        return;
    }

    if (!seats || seats < 1) {
        showToast('Please enter a valid number of seats', 'error');
        return;
    }

    try {
        console.log('[DEBUG] Creating booking:', {
            rideId: currentRideForBooking.id,
            passengerId: passengerId,
            numberOfSeats: parseInt(seats)
        });

        const response = await fetch(API_URLS.BOOKING, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                rideId: currentRideForBooking.id,
                passengerId: passengerId,
                numberOfSeats: parseInt(seats)
            })
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: 'Booking failed' }));
            console.error('[ERROR] Booking response:', err);
            throw new Error(err.error || err.message || 'Booking failed');
        }

        const booking = await response.json();
        console.log('[SUCCESS] Booking created:', booking);
        
        closeModal('booking-modal');
        showToast('Booking Confirmed!', 'success');

        openPaymentModal(booking);
        searchRides();
    } catch (error) {
        console.error('[ERROR] confirmBooking:', error);
        showToast(error.message, 'error');
    }
}

// ===== PAYMENT =====
async function openPaymentModal(booking) {
    currentBookingForPayment = booking;
    
    try {
        // Récupérer les infos du ride
        const rideResponse = await fetch(`${API_URLS.RIDE}/${booking.rideId}`);
        
        let rideInfo = {
            departure: 'N/A',
            destination: 'N/A',
            driverName: 'N/A',
            driverId: null,
            price: 0
        };
        
        if (rideResponse.ok) {
            const ride = await rideResponse.json();
            rideInfo = {
                departure: ride.departure || 'N/A',
                destination: ride.destination || 'N/A',
                driverName: ride.driverName || 'N/A',
                driverId: ride.driverId || null,
                price: ride.price || 0
            };
        }
        
        // Calculer le montant total
        const totalAmount = rideInfo.price * booking.numberOfSeats;
        
        // Stocker les infos complètes
        currentBookingForPayment = {
            ...booking,
            rideInfo: rideInfo,
            totalAmount: totalAmount
        };

        const html = `
            <div style="margin-bottom: 20px; padding: 15px; background: rgba(99, 102, 241, 0.1); border-radius: 8px;">
                <h3 style="margin: 0 0 10px 0; color: var(--primary);">${rideInfo.departure} → ${rideInfo.destination}</h3>
                <p style="margin: 5px 0;"><strong>Driver:</strong> ${rideInfo.driverName}</p>
            </div>
            
            <div style="margin-bottom: 20px;">
                <p><strong>Booking ID:</strong> ${booking.id}</p>
                <p><strong>Passenger:</strong> ${booking.passengerId}</p>
                <p><strong>Booking Date:</strong> ${new Date(booking.bookingTime).toLocaleString()}</p>
            </div>
            
            <div style="margin-bottom: 20px; padding: 15px; background: rgba(16, 185, 129, 0.1); border-radius: 8px;">
                <p style="margin: 5px 0;"><strong>Price per Seat:</strong> ${rideInfo.price} MRU</p>
                <p style="margin: 5px 0;"><strong>Number of Seats:</strong> ${booking.numberOfSeats}</p>
                <p style="margin: 10px 0 0 0; font-size: 1.2em; color: var(--success);"><strong>Total Amount:</strong> ${totalAmount} MRU</p>
            </div>
        `;

        document.getElementById('payment-booking-details').innerHTML = html;
        document.getElementById('payment-modal').style.display = 'flex';
    } catch (error) {
        console.error('[ERROR] openPaymentModal:', error);
        showToast('Failed to load payment details', 'error');
    }
}

async function processPayment() {
    // Récupérer le driverId automatiquement depuis les infos du ride
    const driverId = currentBookingForPayment.rideInfo?.driverId;

    if (!driverId) {
        showToast('Driver information not available', 'error');
        return;
    }

    try {
        console.log('[DEBUG] Processing payment:', {
            bookingId: currentBookingForPayment.id,
            driverId: driverId,
            totalAmount: currentBookingForPayment.totalAmount
        });

        const response = await fetch(`${API_URLS.PAYMENT}/from-booking`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                bookingId: currentBookingForPayment.id,
                driverId: driverId
            })
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: 'Payment failed' }));
            console.error('[ERROR] Payment response:', err);
            throw new Error(err.error || err.message || 'Payment failed');
        }

        const payment = await response.json();
        console.log('[SUCCESS] Payment processed:', payment);
        
        closeModal('payment-modal');
        
        // Afficher le montant correct (soit depuis le backend, soit calculé localement)
        const displayAmount = payment.amount || payment.sharedAmount || currentBookingForPayment.totalAmount;
        showToast(`Payment Successful! Amount: ${displayAmount} MRU`, 'success');
    } catch (error) {
        console.error('[ERROR] processPayment:', error);
        showToast(error.message, 'error');
    }
}

// ===== MY BOOKINGS =====
async function loadAllMyBookings() {
    const list = document.getElementById('bookings-list');
    if (!list) return;
    
    list.innerHTML = '<div class="loading-state">Loading your bookings...</div>';
    
    try {
        console.log('[DEBUG] Loading all bookings');
        
        // Récupérer tous les bookings
        const response = await fetch(`${API_URLS.BOOKING}/all`);
        
        if (!response.ok) {
            throw new Error('Failed to load bookings');
        }
        
        const bookings = await response.json();
        console.log('[DEBUG] All bookings loaded:', bookings);
        
        if (bookings.length === 0) {
            list.innerHTML = '<div class="loading-state">No bookings found. Book a ride to see it here!</div>';
            return;
        }
        
        // Enrichir chaque booking avec les infos du ride
        const enrichedBookings = await Promise.all(bookings.map(async (booking) => {
            try {
                const rideResponse = await fetch(`${API_URLS.RIDE}/${booking.rideId}`);
                if (rideResponse.ok) {
                    const ride = await rideResponse.json();
                    return {
                        ...booking,
                        rideInfo: {
                            departure: ride.departure || 'N/A',
                            destination: ride.destination || 'N/A',
                            driverName: ride.driverName || 'N/A',
                            departureTime: ride.departureTime || null
                        }
                    };
                }
            } catch (error) {
                console.warn('[WARN] Could not fetch ride info for booking', booking.id);
            }
            // Si échec, retourne le booking sans enrichissement
            return {
                ...booking,
                rideInfo: {
                    departure: 'N/A',
                    destination: 'N/A',
                    driverName: 'N/A',
                    departureTime: null
                }
            };
        }));
        
        console.log('[DEBUG] Enriched bookings:', enrichedBookings);
        
        // Afficher les bookings enrichis
        renderBookings(enrichedBookings);
    } catch (error) {
        console.error('[ERROR] loadAllMyBookings:', error);
        list.innerHTML = '<div class="loading-state">Failed to load bookings. Please try again.</div>';
    }
}

async function loadMyBookings() {
    const city = document.getElementById('my-bookings-city').value;
    if (!city) {
        showToast('Please enter a city name', 'error');
        return;
    }

    const list = document.getElementById('bookings-list');
    list.innerHTML = '<div class="loading-state">Loading...</div>';

    try {
        console.log('[DEBUG] Loading bookings for city:', city);
        const response = await fetch(`${API_URLS.BOOKING}/search?city=${encodeURIComponent(city)}`);

        if (!response.ok) {
            throw new Error(`Failed to load bookings: ${response.statusText}`);
        }

        const bookings = await response.json();
        console.log('[DEBUG] Bookings received:', bookings);

        if (!Array.isArray(bookings)) {
            throw new Error("Invalid response format");
        }

        if (bookings.length === 0) {
            list.innerHTML = '<div class="loading-state">No bookings found for this city.</div>';
            return;
        }

        list.innerHTML = bookings.map(b => `
            <div class="card">
                <div class="card-header">
                    <div class="route">Booking #${b.id}</div>
                    <div class="status-badge ${b.status === 'CONFIRMED' ? 'available' : 'full'}">${b.status}</div>
                </div>
                <div class="card-details">
                    <div class="detail-row"><span>Ride ID</span><span>${b.rideId}</span></div>
                    <div class="detail-row"><span>Seats</span><span>${b.numberOfSeats}</span></div>
                    <div class="detail-row"><span>Date</span><span>${new Date(b.bookingTime).toLocaleDateString()}</span></div>
                </div>
                ${b.status === 'CONFIRMED' ?
                `<button class="action-btn full-width" style="background-color: var(--error);" onclick="cancelBooking(${b.id})">Cancel Booking</button>`
                : ''}
            </div>
        `).join('');
    } catch (error) {
        console.error('[ERROR] loadMyBookings:', error);
        list.innerHTML = `<div class="loading-state" style="color: var(--error)">Error loading bookings: ${error.message}</div>`;
    }
}

function renderBookings(bookings) {
    const list = document.getElementById('bookings-list');
    if (!list) return;
    
    if (bookings.length === 0) {
        list.innerHTML = '<div class="loading-state">No bookings found.</div>';
        return;
    }
    
    list.innerHTML = bookings.map(b => `
        <div class="card">
            <div class="card-header">
                <div class="route">
                    ${b.rideInfo ? `${b.rideInfo.departure} → ${b.rideInfo.destination}` : `Booking #${b.id}`}
                </div>
                <div class="status-badge ${b.status === 'CONFIRMED' ? 'available' : 'full'}">${b.status}</div>
            </div>
            <div class="card-details">
                <div class="detail-row"><span>Booking ID</span><span>#${b.id}</span></div>
                <div class="detail-row"><span>Ride ID</span><span>#${b.rideId}</span></div>
                ${b.rideInfo ? `<div class="detail-row"><span>Driver</span><span>${b.rideInfo.driverName}</span></div>` : ''}
                <div class="detail-row"><span>Passenger</span><span>${b.passengerId}</span></div>
                <div class="detail-row"><span>Seats</span><span>${b.numberOfSeats}</span></div>
                <div class="detail-row"><span>Booked On</span><span>${new Date(b.bookingTime).toLocaleDateString()}</span></div>
            </div>
            ${b.status === 'CONFIRMED' ? `
                <button class="action-btn full-width" style="background-color: #d5a761; border: none;" onclick="cancelBooking(${b.id})">
                    Cancel Booking
                </button>
            ` : b.status === 'CANCELLED' ? `
                <button class="delete-btn full-width" style="background-color: #752f36; border: none;" onclick="deleteBooking(${b.id})">
                    Delete
                </button>
            ` : ''}
        </div>
    `).join('');
}

async function cancelBooking(id) {
    if (!confirm('Are you sure you want to cancel this booking?')) return;

    try {
        console.log('[DEBUG] Cancelling booking:', id);
        const response = await fetch(`${API_URLS.BOOKING}/${id}/cancel`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: 'Cancellation failed' }));
            console.error('[ERROR] Cancellation response:', err);
            throw new Error(err.error || err.message || 'Cancellation failed');
        }

        console.log('[SUCCESS] Booking cancelled');
        showToast('Booking Cancelled', 'success');
        loadAllMyBookings();
    } catch (error) {
        console.error('[ERROR] cancelBooking:', error);
        showToast(error.message, 'error');
    }
}

async function deleteBooking(id) {
    if (!confirm('Are you sure you want to permanently delete this booking?')) return;

    try {
        console.log('[DEBUG] Deleting booking:', id);
        const response = await fetch(`${API_URLS.BOOKING}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: 'Deletion failed' }));
            console.error('[ERROR] Deletion response:', err);
            throw new Error(err.error || err.message || 'Deletion failed');
        }

        console.log('[SUCCESS] Booking deleted');
        showToast('Booking Deleted Successfully', 'success');
        loadAllMyBookings();
    } catch (error) {
        console.error('[ERROR] deleteBooking:', error);
        showToast(error.message, 'error');
    }
}

// ===== EVALUATIONS =====
async function loadAllBookingsForEvaluation() {
    try {
        console.log('[DEBUG] Loading all bookings for evaluation');
        
        const response = await fetch(`${API_URLS.BOOKING}/all`);
        
        if (!response.ok) {
            throw new Error('Failed to load bookings');
        }
        
        const bookings = await response.json();
        console.log('[DEBUG] Bookings loaded:', bookings);
        
        const select = document.getElementById('eval-booking-id');
        if (!select) return;
        
        select.innerHTML = '<option value="">Select a booking...</option>';
        
        bookings.forEach(booking => {
            const option = document.createElement('option');
            option.value = booking.id;
            option.textContent = `Booking #${booking.id} - Ride #${booking.rideId} - Passenger: ${booking.passengerId}`;
            select.appendChild(option);
        });
        
        console.log('[DEBUG] Booking dropdown populated with', bookings.length, 'bookings');
    } catch (error) {
        console.error('[ERROR] loadAllBookingsForEvaluation:', error);
        showToast('Failed to load bookings', 'error');
    }
}

async function loadBookingDetails() {
    const bookingId = document.getElementById('eval-booking-id').value;
    
    if (!bookingId) {
        // Reset fields
        currentEvaluationInfo = null;
        document.getElementById('eval-ride-id').value = '';
        document.getElementById('eval-evaluator-id').value = '';
        document.getElementById('eval-evaluator-id-real').value = '';
        document.getElementById('eval-evaluated-id').value = '';
        document.getElementById('eval-evaluated-id-real').value = '';
        return;
    }
    
    try {
        console.log('[DEBUG] Loading details for booking:', bookingId);
        
        const response = await fetch(`${API_URLS.BOOKING}/${bookingId}/evaluation-info`);
        
        if (!response.ok) {
            throw new Error('Failed to load booking details');
        }
        
        const info = await response.json();
        console.log('[DEBUG] Booking details received:', info);
        
        // Vérifier et loguer les valeurs du driver
        console.log('[DEBUG] Driver ID:', info.driverId);
        console.log('[DEBUG] Driver Name:', info.driverName);
        
        // Utiliser driverId comme fallback si driverName est vide
        const driverDisplayName = info.driverName && info.driverName.trim() !== '' 
            ? info.driverName 
            : info.driverId || 'Unknown Driver';
        
        console.log('[DEBUG] Driver display name will be:', driverDisplayName);
        
        // Stocker les infos complètes
        currentEvaluationInfo = {
            bookingId: info.bookingId,
            rideId: info.rideId,
            passengerId: info.passengerId || 'Unknown',
            driverId: info.driverId || 'UNKNOWN',
            driverName: driverDisplayName
        };
        
        console.log('[DEBUG] currentEvaluationInfo set to:', currentEvaluationInfo);
        
        // Remplir Ride ID
        document.getElementById('eval-ride-id').value = info.rideId;
        
        // Mettre à jour les champs selon le type d'évaluation
        updateEvaluationFields();
        
        showToast('Booking details loaded', 'info');
    } catch (error) {
        console.error('[ERROR] loadBookingDetails:', error);
        showToast('Failed to load booking details', 'error');
    }
}

// Nouvelle fonction pour mettre à jour evaluator et evaluated selon le type
function updateEvaluationFields() {
    if (!currentEvaluationInfo) {
        console.log('[DEBUG] currentEvaluationInfo is null');
        return;
    }
    
    console.log('[DEBUG] currentEvaluationInfo:', currentEvaluationInfo);
    
    const evaluationType = document.getElementById('eval-type').value;
    
    // S'assurer qu'on a un driver name, sinon utiliser l'ID
    const driverDisplayName = currentEvaluationInfo.driverName || currentEvaluationInfo.driverId || 'N/A';
    const passengerDisplayName = currentEvaluationInfo.passengerId || 'N/A';
    
    console.log('[DEBUG] Driver display name:', driverDisplayName);
    console.log('[DEBUG] Passenger display name:', passengerDisplayName);
    
    if (evaluationType === 'DRIVER') {
        // Évaluation du DRIVER
        // Evaluator = Passenger, Evaluated = Driver
        document.getElementById('eval-evaluator-id').value = passengerDisplayName;
        document.getElementById('eval-evaluator-id-real').value = currentEvaluationInfo.passengerId;
        
        document.getElementById('eval-evaluated-id').value = driverDisplayName;
        document.getElementById('eval-evaluated-id-real').value = currentEvaluationInfo.driverId;
        
        console.log('[DEBUG] DRIVER evaluation: Passenger evaluates Driver');
        console.log('[DEBUG] Evaluator (visible):', passengerDisplayName);
        console.log('[DEBUG] Evaluated (visible):', driverDisplayName);
    } else if (evaluationType === 'PASSENGER') {
        // Évaluation du PASSENGER
        // Evaluator = Driver, Evaluated = Passenger
        document.getElementById('eval-evaluator-id').value = driverDisplayName;
        document.getElementById('eval-evaluator-id-real').value = currentEvaluationInfo.driverId;
        
        document.getElementById('eval-evaluated-id').value = passengerDisplayName;
        document.getElementById('eval-evaluated-id-real').value = currentEvaluationInfo.passengerId;
        
        console.log('[DEBUG] PASSENGER evaluation: Driver evaluates Passenger');
        console.log('[DEBUG] Evaluator (visible):', driverDisplayName);
        console.log('[DEBUG] Evaluated (visible):', passengerDisplayName);
    }
}

async function submitEvaluation() {
    const bookingId = document.getElementById('eval-booking-id').value;
    const rideId = document.getElementById('eval-ride-id').value;
    const evaluatorIdReal = document.getElementById('eval-evaluator-id-real').value;
    const evaluatedIdReal = document.getElementById('eval-evaluated-id-real').value;
    const rating = document.getElementById('eval-rating').value;
    const type = document.getElementById('eval-type').value;
    const comment = document.getElementById('eval-comment').value;

    if (!bookingId || !evaluatorIdReal || !evaluatedIdReal || !rating || !type) {
        showToast('Please select a booking and fill in all required fields', 'error');
        return;
    }

    try {
        const payload = {
            evaluatorId: evaluatorIdReal,  // Utilise le vrai ID
            evaluatedId: evaluatedIdReal,  // Utilise le vrai ID
            rating: parseInt(rating),
            type: type,
            comment: comment || null
        };

        // Add optional fields
        if (bookingId) payload.bookingId = parseInt(bookingId);
        if (rideId) payload.rideId = parseInt(rideId);

        console.log('[DEBUG] Submitting evaluation:', payload);

        const response = await fetch(API_URLS.EVALUATION, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: 'Evaluation failed' }));
            throw new Error(err.error || err.message || 'Evaluation failed');
        }

        const evaluation = await response.json();
        console.log('[SUCCESS] Evaluation submitted:', evaluation);
        
        showToast('Evaluation submitted successfully!', 'success');
        
        // Clear form
        document.getElementById('eval-booking-id').value = '';
        document.getElementById('eval-ride-id').value = '';
        document.getElementById('eval-evaluator-id').value = '';
        document.getElementById('eval-evaluator-id-real').value = '';
        document.getElementById('eval-evaluated-id').value = '';
        document.getElementById('eval-evaluated-id-real').value = '';
        document.getElementById('eval-rating').value = '5';
        document.getElementById('eval-type').value = 'DRIVER';
        document.getElementById('eval-comment').value = '';
        
        // Reset currentEvaluationInfo
        currentEvaluationInfo = null;
    } catch (error) {
        console.error('[ERROR] submitEvaluation:', error);
        showToast(error.message, 'error');
    }
}

async function loadRecentEvaluations() {
    const list = document.getElementById('evaluations-list');
    list.innerHTML = '<div class="loading-state">Loading evaluations...</div>';

    try {
        console.log('[DEBUG] Loading recent evaluations');
        
        const response = await fetch(API_URLS.EVALUATION);
        
        if (!response.ok) {
            throw new Error('Failed to load evaluations');
        }

        const evaluations = await response.json();
        console.log('[DEBUG] Evaluations loaded:', evaluations);

        if (!Array.isArray(evaluations) || evaluations.length === 0) {
            list.innerHTML = '<div class="loading-state">No evaluations found.</div>';
            return;
        }

        list.innerHTML = evaluations.map(evaluation => `
            <div class="card">
                <div class="card-header">
                    <div class="route">Evaluation #${evaluation.id}</div>
                    <div class="rating-display">${evaluation.rating}/5</div>
                </div>
                <div class="card-details">
                    ${evaluation.bookingId ? `<div class="detail-row"><span>Booking ID</span><span>${evaluation.bookingId}</span></div>` : ''}
                    ${evaluation.rideId ? `<div class="detail-row"><span>Ride ID</span><span>${evaluation.rideId}</span></div>` : ''}
                    <div class="detail-row"><span>Evaluator</span><span>${evaluation.evaluatorId}</span></div>
                    <div class="detail-row"><span>Evaluated</span><span>${evaluation.evaluatedId}</span></div>
                    <div class="detail-row"><span>Rating</span><span>${evaluation.rating}/5</span></div>
                    ${evaluation.type ? `<div class="detail-row"><span>Type</span><span class="type-badge ${evaluation.type.toLowerCase()}">${evaluation.type}</span></div>` : ''}
                    ${evaluation.comment ? `<div class="detail-row full-width"><span>Comment:</span><span>${evaluation.comment}</span></div>` : ''}
                    <div class="detail-row"><span>Date</span><span>${new Date(evaluation.evaluationTime || Date.now()).toLocaleDateString()}</span></div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('[ERROR] loadRecentEvaluations:', error);
        list.innerHTML = `<div class="loading-state" style="color: var(--error)">Error loading evaluations: ${error.message}</div>`;
    }
}

async function loadBookingsForEvaluationSearch() {
    try {
        console.log('[DEBUG] Loading bookings for evaluation search');
        
        const response = await fetch(`${API_URLS.BOOKING}/all`);
        
        if (!response.ok) {
            throw new Error('Failed to load bookings');
        }
        
        const bookings = await response.json();
        console.log('[DEBUG] Bookings loaded for search:', bookings);
        
        const select = document.getElementById('search-booking-evaluations');
        if (!select) return;
        
        select.innerHTML = '<option value="">Select a booking...</option>';
        
        bookings.forEach(booking => {
            const option = document.createElement('option');
            option.value = booking.id;
            option.textContent = `Booking #${booking.id} - Ride #${booking.rideId} - Passenger: ${booking.passengerId}`;
            select.appendChild(option);
        });
        
        console.log('[DEBUG] Search booking dropdown populated with', bookings.length, 'bookings');
    } catch (error) {
        console.error('[ERROR] loadBookingsForEvaluationSearch:', error);
    }
}

async function getBookingEvaluations() {
    const bookingId = document.getElementById('search-booking-evaluations').value;
    
    if (!bookingId) {
        showToast('Please select a booking', 'error');
        return;
    }

    const list = document.getElementById('evaluations-list');
    list.innerHTML = '<div class="loading-state">Loading booking evaluations...</div>';

    try {
        console.log('[DEBUG] Loading evaluations for booking:', bookingId);
        
        const response = await fetch(`${API_URLS.EVALUATION}/booking/${bookingId}`);
        
        if (!response.ok) {
            throw new Error('Failed to load booking evaluations');
        }

        const evaluations = await response.json();
        console.log('[DEBUG] Booking evaluations loaded:', evaluations);

        if (!Array.isArray(evaluations) || evaluations.length === 0) {
            list.innerHTML = '<div class="loading-state">No evaluations found for this booking.</div>';
            return;
        }

        list.innerHTML = evaluations.map(evaluation => `
            <div class="card">
                <div class="card-header">
                    <div class="route">Evaluation #${evaluation.id}</div>
                    <div class="rating-display">${evaluation.rating}/5</div>
                </div>
                <div class="card-details">
                    ${evaluation.bookingId ? `<div class="detail-row"><span>Booking ID</span><span>${evaluation.bookingId}</span></div>` : ''}
                    ${evaluation.rideId ? `<div class="detail-row"><span>Ride ID</span><span>${evaluation.rideId}</span></div>` : ''}
                    <div class="detail-row"><span>Evaluator</span><span>${evaluation.evaluatorId}</span></div>
                    <div class="detail-row"><span>Evaluated</span><span>${evaluation.evaluatedId}</span></div>
                    <div class="detail-row"><span>Rating</span><span>${evaluation.rating}/5</span></div>
                    ${evaluation.type ? `<div class="detail-row"><span>Type</span><span class="type-badge ${evaluation.type.toLowerCase()}">${evaluation.type}</span></div>` : ''}
                    ${evaluation.comment ? `<div class="detail-row full-width"><span>Comment:</span><span>${evaluation.comment}</span></div>` : ''}
                    <div class="detail-row"><span>Date</span><span>${new Date(evaluation.evaluationTime || Date.now()).toLocaleDateString()}</span></div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('[ERROR] getBookingEvaluations:', error);
        list.innerHTML = `<div class="loading-state" style="color: var(--error)">Error: ${error.message}</div>`;
    }
}

async function filterByType() {
    const type = document.getElementById('filter-evaluation-type').value;
    
    if (!type) {
        loadRecentEvaluations();
        return;
    }

    const list = document.getElementById('evaluations-list');
    list.innerHTML = '<div class="loading-state">Loading evaluations...</div>';

    try {
        console.log('[DEBUG] Filtering evaluations by type:', type);
        
        const response = await fetch(`${API_URLS.EVALUATION}/type/${type}`);
        
        if (!response.ok) {
            throw new Error('Failed to load evaluations');
        }

        const evaluations = await response.json();
        console.log('[DEBUG] Filtered evaluations loaded:', evaluations);

        if (!Array.isArray(evaluations) || evaluations.length === 0) {
            list.innerHTML = `<div class="loading-state">No ${type} evaluations found.</div>`;
            return;
        }

        list.innerHTML = evaluations.map(evaluation => `
            <div class="card">
                <div class="card-header">
                    <div class="route">Evaluation #${evaluation.id}</div>
                    <div class="rating-display">${evaluation.rating}/5</div>
                </div>
                <div class="card-details">
                    ${evaluation.bookingId ? `<div class="detail-row"><span>Booking ID</span><span>${evaluation.bookingId}</span></div>` : ''}
                    ${evaluation.rideId ? `<div class="detail-row"><span>Ride ID</span><span>${evaluation.rideId}</span></div>` : ''}
                    <div class="detail-row"><span>Evaluator</span><span>${evaluation.evaluatorId}</span></div>
                    <div class="detail-row"><span>Evaluated</span><span>${evaluation.evaluatedId}</span></div>
                    <div class="detail-row"><span>Rating</span><span>${evaluation.rating}/5</span></div>
                    <div class="detail-row"><span>Type</span><span class="type-badge ${evaluation.type.toLowerCase()}">${evaluation.type}</span></div>
                    ${evaluation.comment ? `<div class="detail-row full-width"><span>Comment:</span><span>${evaluation.comment}</span></div>` : ''}
                    <div class="detail-row"><span>Date</span><span>${new Date(evaluation.evaluationTime || Date.now()).toLocaleDateString()}</span></div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('[ERROR] filterByType:', error);
        list.innerHTML = `<div class="loading-state" style="color: var(--error)">Error: ${error.message}</div>`;
    }
}

// ===== UTILS =====
function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

function showToast(msg, type) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}

// ===== INIT =====
console.log('[INFO] App initialized');
console.log('[INFO] API URLs:', API_URLS);
loadDrivers();
searchRides();