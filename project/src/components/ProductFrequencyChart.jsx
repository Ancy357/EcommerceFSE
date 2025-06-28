import React, { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from 'chart.js';
import { fetchProductFrequencyStats } from '../services/adminService';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const ProductFrequencyChart = () => {
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [timeFrame, setTimeFrame] = useState('all');

  useEffect(() => {
    fetchProductFrequencyStats()
      .then((res) => setStats(res.data))
      .catch((err) => console.error('Failed to fetch product frequency:', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="text-center py-4" style={{ fontFamily: 'Georgia, serif' }}>Loading chart...</div>;
  }

  const labels =
    timeFrame === 'all'
      ? ['Week', 'Month', 'Year']
      : [timeFrame.charAt(0).toUpperCase() + timeFrame.slice(1)];

  const datasets = Object.entries(stats).map(([productName, stat], index) => {
    let data;
    switch (timeFrame) {
      case 'week':
        data = [stat.week];
        break;
      case 'month':
        data = [stat.month];
        break;
      case 'year':
        data = [stat.year];
        break;
      default:
        data = [stat.week, stat.month, stat.year];
    }

    return {
      label: productName,
      data,
      backgroundColor: `rgba(${(index * 60) % 255}, ${(index * 100) % 255}, ${(index * 160) % 255}, 0.6)`,
      borderColor: '#333',
      borderWidth: 1
    };
  });

  const data = { labels, datasets };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: 20 },
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          boxWidth: 16,
          padding: 8,
          font: { family: 'Georgia' }
        }
      },
      tooltip: {
        mode: 'index',
        intersect: false
      }
    },
    scales: {
      x: {
        ticks: {
          autoSkip: false,
          maxRotation: 45,
          minRotation: 30,
          font: { family: 'Georgia' }
        },
        title: {
          display: true,
          text: 'Time Frame',
          font: { family: 'Georgia', size: 14 }
        }
      },
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Order Count',
          font: { family: 'Georgia', size: 14 }
        },
        ticks: {
          precision: 0,
          font: { family: 'Georgia' }
        }
      }
    }
  };

  return (
    <div className="p-4 bg-light shadow rounded" style={{ marginTop: '6rem', fontFamily: 'Georgia, serif' }}>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h4 className="mb-0"> Product Order Frequency</h4>
        <div>
          {['all', 'week', 'month', 'year'].map((frame) => (
            <button
              key={frame}
              onClick={() => setTimeFrame(frame)}
              className="btn btn-sm me-2 mb-1"
              style={{
                backgroundColor: timeFrame === frame ? '#B57B5B' : 'transparent',
                color: timeFrame === frame ? '#fff' : '#B57B5B',
                border: '1px solid #B57B5B',
                fontFamily: 'Georgia, serif',
                transition: 'all 0.2s ease'
              }}
            >
              {frame.charAt(0).toUpperCase() + frame.slice(1)}
            </button>
          ))}
        </div>
      </div>

      <div style={{ overflowX: 'auto', minHeight: '420px' }}>
        <Bar data={data} options={options} height={400} />
      </div>
    </div>
  );
};

export default ProductFrequencyChart;
