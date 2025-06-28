import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  getAddressesByUser,
  placeOrderFromCartCashOnDelivery,
  startCartOnlinePayment,
} from '../services/serviceapi';
import GoBackButton from './GoBackButton';
import { useAuth } from '../contexts/AuthContext';

function CartCheckout() {
  const [paymentMethod, setPaymentMethod] = useState('');
  const [upiId, setUpiId] = useState('');
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  const totalPrice = location.state?.totalPrice || 0;
  const { user } = useAuth();
const userId = user?.id;

useEffect(() => {
    if (!userId) return; // 🛡️ Wait until userId is defined
  
    getAddressesByUser(userId)
      .then((res) => setAddresses(res.data))
      .catch((err) => {
        console.error('Error fetching addresses:', err);
        setError('Could not fetch your addresses.');
      });
  }, [userId]);
  

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedAddress) {
      setError('Please select a shipping address.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      if (paymentMethod === 'cod') {
        const res = await placeOrderFromCartCashOnDelivery(userId, selectedAddress);
        navigate(`/cod-order-summary/${res.data.orderId}`);
      } else if (paymentMethod === 'online') {
        if (!upiId.includes('@ybl') && !upiId.includes('@ibl')) {
          setError('Enter a valid UPI ID ending with @ybl or @ibl');
          setLoading(false);
          return;
        }
        const cartOnlineDTO = { userId, addressId: selectedAddress, upiId };
        const res = await startCartOnlinePayment(cartOnlineDTO);
        navigate(`/update-payment-status/${res.data.paymentId}`, {
          state: { orderType: 'cart' },
        });
      } else {
        setError('Please select a payment method.');
        setLoading(false);
      }
    } catch (err) {
      console.error('Order processing failed:', err);
      setError('Something went wrong while placing the order.');
      setLoading(false);
    }
  };

  return (
    <div
      className="container"
      style={{ fontFamily: 'Georgia, serif', marginTop: '6rem' }}
    >
      <div className="row align-items-center mb-4">
        <div className="col-md-1 d-flex justify-content-end pe-0">
          <GoBackButton />
        </div>
        <div className="col-md-10 ps-4">
          <h2 className="mb-0">Checkout </h2>
        </div>
      </div>

      <div className="row">
        <div className="col-md-8 offset-md-2">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Order Total</h5>
              <p className="card-text fs-4">
                <strong>Total:</strong> ₹{totalPrice.toFixed(2)}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Select Address</label>
              <select
                className="form-select"
                value={selectedAddress}
                onChange={(e) => setSelectedAddress(e.target.value)}
                required
              >
                <option value="">-- Select Address --</option>
                {addresses.map((addr) => (
                  <option key={addr.id} value={addr.id}>
                    {addr.street}, {addr.city}, {addr.state}, {addr.country},{' '}
                    {addr.postalCode}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label">Payment Method</label>
              <div>
                <button
                  type="button"
                  className="btn me-2"
                  style={{
                    backgroundColor: paymentMethod === 'cod' ? '#B57B5B' : 'transparent',
                    border: '1px solid #B57B5B',
                    color: paymentMethod === 'cod' ? 'white' : '#B57B5B',
                  }}
                  onClick={() => setPaymentMethod('cod')}
                >
                  Cash on Delivery
                </button>
                <button
                  type="button"
                  className="btn"
                  style={{
                    backgroundColor: paymentMethod === 'online' ? '#B57B5B' : 'transparent',
                    border: '1px solid #B57B5B',
                    color: paymentMethod === 'online' ? 'white' : '#B57B5B',
                  }}
                  onClick={() => setPaymentMethod('online')}
                >
                  Online Payment
                </button>
              </div>
            </div>

            {paymentMethod === 'online' && (
              <div className="mb-3">
                <label className="form-label">UPI ID</label>
                <input
                  type="text"
                  className="form-control"
                  value={upiId}
                  onChange={(e) => setUpiId(e.target.value)}
                  required
                />
              </div>
            )}

            {error && <div className="alert alert-danger mt-3">{error}</div>}

            <button
              className="btn w-100"
              type="submit"
              disabled={loading || !totalPrice}
              style={{
                backgroundColor: '#B57B5B',
                borderColor: '#B57B5B',
                color: 'white',
              }}
            >
              {loading ? 'Processing...' : 'Place Order'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default CartCheckout;
