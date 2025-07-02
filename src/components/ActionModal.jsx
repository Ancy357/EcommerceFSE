import React, { useState } from 'react';
import { Modal, Button, Form, Alert, Row, Col } from 'react-bootstrap';
import { returnProduct, replaceProduct } from '../services/orderService';
import { useAuth } from '../contexts/AuthContext';
 
const ActionModal = ({
  show,
  handleClose,
  product,
  orderId,
  paymentMethod,
  onActionSuccess,
  mode
}) => {
  const { user } = useAuth();
  const [quantity, setQuantity] = useState(1);
  const [upiId, setUpiId] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
 
  if (!product) return null;
 
  const capitalizedMode = mode?.charAt(0).toUpperCase() + mode?.slice(1);
 
  const handleReturn = async () => {
    if (quantity <= 0 || quantity > product.quantity) {
      return setError(`Please select quantity between 1 and ${product.quantity}`);
    }
 
    if (paymentMethod?.toLowerCase() === 'cash on delivery' && !upiId.trim()) {
      return setError('UPI ID is required for Cash on Delivery refunds.');
    }
 
    try {
      setSubmitting(true);
      const payload = {
        orderId,
        productId: product.productId,
        quantity,
        upiId,
        userId: user?.id
      };
      await returnProduct(payload);
      onActionSuccess(product.productId, quantity, mode);
      handleClose();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Return failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };
 
  const handleReplace = async () => {
    if (quantity <= 0 || quantity > product.quantity) {
      return setError(`Please select quantity between 1 and ${product.quantity}`);
    }
 
    try {
      setSubmitting(true);
      const payload = {
        orderId,
        productId: product.productId,
        quantity,
        userId: user?.id
      };
      await replaceProduct(payload);
      onActionSuccess(product.productId, quantity, mode);
      handleClose();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Replacement failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };
 
  const handleAction = () => {
    setError('');
    if (mode === 'return') {
      handleReturn();
    } else if (mode === 'replace') {
      handleReplace();
    }
  };
 
  return (
    <Modal show={show} onHide={handleClose} centered backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>{capitalizedMode} Product</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row className="mb-3">
          <Col md={4}>
            <img
              src={product.imageUrl}
              alt={product.productName}
              className="img-fluid rounded"
            />
          </Col>
          <Col md={8}>
            <h6>{product.productName}</h6>
            <p className="mb-1">
              <strong>Quantity Ordered:</strong> {product.quantity}
            </p>
 
            <Form.Group className="mb-2">
              <Form.Label>Quantity to {capitalizedMode}</Form.Label>
              <Form.Control
                type="number"
                min={1}
                max={product.quantity}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
              />
            </Form.Group>
 
            {paymentMethod?.toLowerCase() === 'cash on delivery' && mode === 'return' && (
              <Form.Group className="mb-2">
                <Form.Label>UPI ID for Refund</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="e.g., yourname@upi"
                  value={upiId}
                  onChange={(e) => setUpiId(e.target.value)}
                />
              </Form.Group>
            )}
          </Col>
        </Row>
 
        {error && <Alert variant="danger">{error}</Alert>}
      </Modal.Body>
 
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose} disabled={submitting}>
          Cancel
        </Button>
        <Button
          variant="primary"
          onClick={handleAction}
          disabled={submitting}
          style={{ background: '#B57B5B' }}
        >
          {submitting ? 'Processing...' : `Confirm ${capitalizedMode}`}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};
 
export default ActionModal;