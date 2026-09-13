import { useState } from 'react';
import './App.css';

function App() {
  const [connected, setConnected] = useState(false);

  const toggleConnection = () => {
    setConnected(!connected);
  };

  return (
      <div className="app">
        <header className="header">
          <div>
            <h1>Office VPN</h1>
            <p>Secure Remote Access</p>
          </div>

          <div className={`status-badge ${connected ? 'connected' : 'disconnected'}`}>
            {connected ? 'Connected' : 'Disconnected'}
          </div>
        </header>

        <main className="dashboard">
          <section className="card connection-card">
            <h2>VPN Connection</h2>

            <div className="connection-status">
              <div className={`status-circle ${connected ? 'online' : 'offline'}`} />

              <div>
                <strong>
                  {connected ? 'VPN Connected' : 'VPN Disconnected'}
                </strong>
                <p>
                  {connected
                      ? 'Your connection is currently secure.'
                      : 'Connect to access the office network.'}
                </p>
              </div>
            </div>

            <button
                className={connected ? 'disconnect-button' : 'connect-button'}
                onClick={toggleConnection}
            >
              {connected ? 'Disconnect' : 'Connect'}
            </button>
          </section>

          <section className="card">
            <h2>Server</h2>

            <div className="info-row">
              <span>Host</span>
              <strong>localhost</strong>
            </div>

            <div className="info-row">
              <span>VPN Port</span>
              <strong>5555</strong>
            </div>

            <div className="info-row">
              <span>Server Status</span>
              <strong className="server-online">Online</strong>
            </div>
          </section>

          <section className="card activity-card">
            <h2>Recent Activity</h2>

            <div className="activity-item">
              <span>Server started</span>
              <span>Port 5555</span>
            </div>

            <div className="activity-item">
              <span>Database</span>
              <span>PostgreSQL connected</span>
            </div>

            <div className="activity-item">
              <span>Current session</span>
              <span>{connected ? 'Connected' : 'Inactive'}</span>
            </div>
          </section>
        </main>
      </div>
  );
}

export default App;
