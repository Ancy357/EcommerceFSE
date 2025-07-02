import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button,
  Modal,
  Table
} from 'react-bootstrap';
import {
  fetchAllUsers,
  fetchOrdersByUserId,
  markOrderAsDelivered
} from '../services/adminService';
import OrderModal from './OrderModal';
 
const AdminPanel = () => {
  const [users, setUsers] = useState([]);
  const [ordersByUser, setOrdersByUser] = useState({});
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [deliveringOrderIds, setDeliveringOrderIds] = useState([]);
  const ordersContainerRef = useRef(null);
  const navigate = useNavigate();
 
  useEffect(() => {
    const loadUsersAndOrders = async () => {
      try {
        const res = await fetchAllUsers();
        setUsers(res.data);
 
        const orderMap = {};
        for (const user of res.data) {
          try {
            const orderRes = await fetchOrdersByUserId(user.userID);
 
            orderMap[user.userID] = orderRes.data.slice().sort((a, b) => {
              const statusPriority = (status) =>
                status === 'Placed' ? 0 : status === 'Pending' ? 1 : 2;
              const diff = statusPriority(a.orderStatus) - statusPriority(b.orderStatus);
              return diff !== 0
                ? diff
                : new Date(b.orderTime) - new Date(a.orderTime);
            });
 
          } catch (err) {
            console.error(`Failed to fetch orders for user ${user.userID}`, err);
            orderMap[user.userID] = '__error__';
          }
        }
 
        setOrdersByUser(orderMap);
      } catch (err) {
        console.error('Failed to load users or orders:', err);
      }
    };
 
    loadUsersAndOrders();
  }, []);
 
  const handleMarkAsDelivered = async (orderId, userId) => {
    setDeliveringOrderIds((prev) => [...prev, orderId]);
    const scrollY = ordersContainerRef.current?.scrollTop || 0;
 
    try {
      await markOrderAsDelivered(orderId);
 
      setTimeout(async () => {
        const updatedOrders = await fetchOrdersByUserId(userId);
 
        const sortedOrders = updatedOrders.data.slice().sort((a, b) => {
          const statusPriority = (status) =>
            status === 'Placed' ? 0 : status === 'Pending' ? 1 : 2;
          const diff = statusPriority(a.orderStatus) - statusPriority(b.orderStatus);
          return diff !== 0
            ? diff
            : new Date(b.orderTime) - new Date(a.orderTime);
        });
 
        setOrdersByUser((prev) => ({
          ...prev,
          [userId]: sortedOrders,
        }));
        setDeliveringOrderIds((prev) => prev.filter((id) => id !== orderId));
 
        setTimeout(() => {
          if (ordersContainerRef.current) {
            ordersContainerRef.current.scrollTop = scrollY;
          }
        }, 0);
      }, 3000);
    } catch (err) {
      console.error('Error updating order:', err);
      setDeliveringOrderIds((prev) => prev.filter((id) => id !== orderId));
    }
  };
 
  return (
    <>
      <style>{`
        * {
          font-family: Georgia, serif;
        }
        .btn-brown {
          background-color: #B57B5B !important;
          border-color: #B57B5B !important;
          color: white !important;
        }
        .btn-brown:hover {
          background-color: #a66d4b !important;
          border-color: #a66d4b !important;
        }
      `}</style>
 
      <div className="container px-5" style={{ paddingTop: '6rem', paddingBottom: '3rem' }}>
        <div className="d-flex justify-content-start mb-3">
          <button onClick={() => navigate('/')} className="btn btn-sm btn-brown">
            <i className="bi bi-arrow-left me-2"></i> Back to Dashboard
          </button>
        </div>
 
        <h2 className="text-center mb-4" style={{ color: 'black' }}>
          Admin Dashboard - All Orders
        </h2>
 
        <div ref={ordersContainerRef} style={{ maxHeight: '80vh', overflowY: 'auto' }}>
          {users.map((user) => (
            <div key={user.userID} className="mb-5">
              <h5 style={{ color: '#5C4033' }}>
                👤 {user.firstName} {user.lastName} — <small>User ID: {user.userID}</small>
              </h5>
 
              {ordersByUser[user.userID] === '__error__' ? (
                <p className="text-danger">❌ Failed to fetch orders for this user.</p>
              ) : ordersByUser[user.userID]?.length > 0 ? (
                <Table striped bordered hover responsive className="mt-2">
                  <thead>
                    <tr>
                      <th>Order ID</th>
                      <th>Status</th>
                      <th>Amount</th>
                      <th>Quantity</th>
                      <th>Payment</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ordersByUser[user.userID].map((order) => (
                      <tr key={order.orderId}>
                        <td
                          style={{ cursor: 'pointer', color: 'black' }}
                          onClick={() => {
                            setSelectedOrder({
                              ...order,
                              userId: user.userID,
                              updateAmountInPanel: (newAmount) => {
                                setOrdersByUser((prev) => {
                                  const updated = [...(prev[user.userID] || [])];
                                  const idx = updated.findIndex(o => o.orderId === order.orderId);
                                  if (idx !== -1) updated[idx].orderAmount = newAmount;
                                  return { ...prev, [user.userID]: updated };
                                });
                              }
                            });
                            setShowModal(true);
                          }}
                        >
                          {order.orderId}
                        </td>
                        <td>{order.orderStatus}</td>
                        <td>₹{order.totalPrice}</td>
                        <td>{order.quantity}</td>
                        <td>{order.paymentStatus}</td>
                        <td>
                          {order.orderStatus === 'Placed' && (
                            <Button
                              size="sm"
                              className="btn-brown"
                              disabled={deliveringOrderIds.includes(order.orderId)}
                              onClick={() => handleMarkAsDelivered(order.orderId, user.userID)}
                            >
                              {deliveringOrderIds.includes(order.orderId) ? (
                                <>
                                  <span
                                    className="spinner-border spinner-border-sm me-2"
                                    role="status"
                                    aria-hidden="true"
                                  ></span>
                                  Delivering...
                                </>
                              ) : (
                                'Mark Delivered'
                              )}
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              ) : (
                <p className="text-muted">No orders found for this user.</p>
              )}
            </div>
          ))}
        </div>
 
        {selectedOrder && (
          <Modal
            show={showModal}
            onHide={() => setShowModal(false)}
            size="lg"
            centered
          >
            <Modal.Header closeButton>
              <Modal.Title>Order Details - {selectedOrder.orderId}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <OrderModal order={selectedOrder} />
            </Modal.Body>
          </Modal>
        )}
      </div>
    </>
  );
};
 
export default AdminPanel;