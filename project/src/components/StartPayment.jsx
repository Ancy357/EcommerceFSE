import React, { useState, useEffect } from 'react';
import {
  getAddressesByUser,
  getProductById,
  startPayment,
  placeCashOnDelivery
} from '../services/serviceapi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useParams } from 'react-router-dom';



function StartPayment() {
  const [quantity, setQuantity] = useState(1);
  const [paymentMethod, setPaymentMethod] = useState('');
  const [upiId, setUpiId] = useState('');
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState('');
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // hard-coded for example; replace with auth/user context if available
  const { user } = useAuth();
const userId = user?.id;

const { productId } = useParams();


  useEffect(() => {
    // fetch all user addresses
    getAddressesByUser(userId)
      .then(res => setAddresses(res.data))
      .catch(err => console.error('Error fetching addresses:', err));

    // fetch product data
    getProductById(productId)
      .then(res => setProduct(res.data))
      .catch(err => console.error('Error fetching product:', err));
  }, [userId, productId]);

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (!selectedAddress) {
        setError('Please select a shipping address.');
        return;
      }

      if (paymentMethod === 'online') {
        const dto = {
          userId,
          productId,
          quantity,
          addressId: Number(selectedAddress),
          upiId: upiId.trim()
        };

        if (!/@(ybl|ibl)$/i.test(dto.upiId)) {
          setError('UPI must end with @ybl or @ibl');
          return;
        }

        const res = await startPayment(dto);
        const paymentId = res.data.paymentDetails.paymentId;
        navigate(`/update-payment-status/${paymentId}`, {
          state: { orderType: 'single' }
        });

      } else if (paymentMethod === 'cod') {
        const dto = {
          userId,
          productId,
          quantity,
          addressId: Number(selectedAddress)
        };

        // Pass userId as first argument
        const res = await placeCashOnDelivery(userId, dto);
        const orderId = res.data.orderDetails.orderId;
        navigate(`/cod-order-summary/${orderId}`);

      } else {
        setError('Please select a payment method.');
        return;
      }
    } catch (err) {
      console.error('Order processing failed:', err);
      setError('Something went wrong while placing the order.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="row">
      <div className="col-md-8 offset-md-2">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h2>Check Out</h2>
          <button
            type="button"
            className="btn btn-info"
            onClick={() => navigate('/cart')}
          >
            Go to Cart
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {product && (
            <div className="card mb-4">
              <div className="card-body text-center">
                <img
                  src={product.imageURL}
                  alt={product.name}
                  style={{ width: 120, height: 120, objectFit: 'contain' }}
                  className="mb-3"
                />
                <h5>{product.name}</h5>
                <div className="d-flex justify-content-center align-items-center mb-3">
                  <button
                    type="button"
                    className="btn btn-outline-secondary me-2"
                    onClick={() =>
                      setQuantity(q => Math.max(1, q - 1))
                    }
                  >
                    –
                  </button>
                  <input
                    type="number"
                    min="1"
                    className="form-control text-center w-25"
                    value={quantity}
                    onChange={e =>
                      setQuantity(Math.max(1, Number(e.target.value)))
                    }
                  />
                  <button
                    type="button"
                    className="btn btn-outline-secondary ms-2"
                    onClick={() => setQuantity(q => q + 1)}
                  >
                    +
                  </button>
                </div>
                <p className="text-muted">{product.description}</p>
                <p>
                  <strong>Subtotal:</strong> ₹{product.price * quantity}
                </p>
              </div>
            </div>
          )}

          <div className="mb-3">
            <label htmlFor="addressSelect" className="form-label">
              Shipping Address
            </label>
            <select
              id="addressSelect"
              className="form-select"
              value={selectedAddress}
              onChange={e => setSelectedAddress(e.target.value)}
              required
            >
              <option value="">-- Select Address --</option>
              {addresses.map(addr => (
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
                className={`btn me-2 ${
                  paymentMethod === 'cod'
                    ? 'btn-secondary'
                    : 'btn-outline-secondary'
                }`}
                onClick={() => setPaymentMethod('cod')}
              >
                Cash on Delivery
              </button>
              <button
                type="button"
                className={`btn ${
                  paymentMethod === 'online'
                    ? 'btn-primary'
                    : 'btn-outline-primary'
                }`}
                onClick={() => setPaymentMethod('online')}
              >
                Online Payment
              </button>
            </div>
          </div>

          {paymentMethod === 'online' && (
            <div className="mb-3">
              <label htmlFor="upiInput" className="form-label">
                UPI ID
              </label>
              <input
                id="upiInput"
                type="text"
                className="form-control"
                value={upiId}
                onChange={e => setUpiId(e.target.value)}
                required
              />
            </div>
          )}

          {error && (
            <div className="alert alert-danger">{error}</div>
          )}

          <button
            type="submit"
            className="btn btn-success w-100"
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Place Order'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default StartPayment;
