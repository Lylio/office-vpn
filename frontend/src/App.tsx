import { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [connected, setConnected] = useState(false);
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('');
  const [connectionMessage, setConnectionMessage] = useState('');
  const [connectionLoading, setConnectionLoading] = useState(false);

  const [apiOnline, setApiOnline] = useState(false);
  const [vpnOnline, setVpnOnline] = useState(false);
  const [databaseOnline, setDatabaseOnline] = useState(false);
  const [statusLoading, setStatusLoading] = useState(true);

  useEffect(() => {
    const checkSystemStatus = async () => {
      try {
        const response = await fetch('/api/status');

        if (!response.ok) {
          throw new Error('Unable to retrieve system status');
        }

        const data = await response.json();

        setApiOnline(data.api === 'UP');
        setVpnOnline(data.vpnServer === 'UP');
        setDatabaseOnline(data.database === 'UP');
      } catch (error) {
        console.error('Status check failed:', error);

        setApiOnline(false);
        setVpnOnline(false);
        setDatabaseOnline(false);
      } finally {
        setStatusLoading(false);
      }
    };

    checkSystemStatus();

    const interval = setInterval(checkSystemStatus, 5000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const checkConnection = async () => {
      try {
        const response = await fetch('/api/connection');

        if (!response.ok) {
          return;
        }

        const data = await response.json();

        setConnected(data.connected);

        if (data.username) {
          setUsername(data.username);
        }

        if (data.role) {
          setRole(data.role);
        }
      } catch (error) {
        console.error(
            'Unable to retrieve connection state:',
            error
        );
      }
    };

    checkConnection();
  }, []);

  const toggleConnection = async () => {
    setConnectionLoading(true);
    setConnectionMessage('');

    try {
      if (connected) {

        const response = await fetch('/api/disconnect', {
          method: 'POST',
        });

        const data = await response.json();

        if (!response.ok) {
          throw new Error(
              data.message || 'Disconnect failed'
          );
        }

        setConnected(false);
        setRole('');
        setConnectionMessage(data.message);

      } else {

        const response = await fetch('/api/connect', {
          method: 'POST',

          headers: {
            'Content-Type': 'application/json',
          },

          body: JSON.stringify({
            username,
            password,
          }),
        });

        const data = await response.json();

        if (!response.ok) {
          throw new Error(
              data.message || 'Connection failed'
          );
        }

        setConnected(data.connected);
        setRole(data.role ?? '');
        setPassword('');

        setConnectionMessage(
            data.message
        );
      }

    } catch (error) {

      console.error(
          'VPN connection error:',
          error
      );

      setConnected(false);
      setPassword('');

      if (error instanceof Error) {
        setConnectionMessage(
            error.message
        );
      } else {
        setConnectionMessage(
            'Unexpected connection error'
        );
      }

    } finally {

      setConnectionLoading(false);

    }
  };

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Office VPN</h1>
          <p>Secure Remote Access</p>
        </div>

        <div
          className={`status-badge ${
            connected ? 'connected' : 'disconnected'
          }`}
        >
          {connected ? 'Connected' : 'Disconnected'}
        </div>
      </header>

      <main className="dashboard">
        <section className="card connection-card">
          <h2>VPN Connection</h2>

          <div className="connection-status">
            <div
                className={`status-circle ${
                    connected ? 'online' : 'offline'
                }`}
            />

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

          {!connected && (
              <div className="login-form">

                <div className="form-group">
                  <label htmlFor="username">Username</label>

                  <input
                      id="username"
                      type="text"
                      value={username}
                      onChange={(event) =>
                          setUsername(event.target.value)
                      }
                      autoComplete="username"
                      placeholder="Enter username"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="password">Password</label>

                  <input
                      id="password"
                      type="password"
                      value={password}
                      onChange={(event) =>
                          setPassword(event.target.value)
                      }
                      autoComplete="current-password"
                      placeholder="Enter password"
                  />
                </div>

              </div>
          )}

          {connected && (
              <div className="session-details">
                <div className="info-row">
                  <span>Authenticated User</span>
                  <strong>{username}</strong>
                </div>

                <div className="info-row">
                  <span>Role</span>
                  <strong>{role}</strong>
                </div>

                <div className="info-row">
                  <span>Encryption</span>
                  <strong>ECDH / AES-256-GCM</strong>
                </div>
              </div>
          )}

          <button
              className={
                connected
                    ? 'disconnect-button'
                    : 'connect-button'
              }
              onClick={toggleConnection}
              disabled={connectionLoading}
          >
            {connectionLoading
                ? 'Working...'
                : connected
                    ? 'Disconnect'
                    : 'Connect'}
          </button>

          {connectionMessage && (
              <p className="connection-message">
                {connectionMessage}
              </p>
          )}
        </section>

        <section className="card">
          <h2>System Status</h2>

          <div className="info-row">
            <span>Web API</span>
            <strong
              className={
                apiOnline
                  ? 'server-online'
                  : 'server-offline'
              }
            >
              {statusLoading
                ? 'Checking...'
                : apiOnline
                  ? '● Online'
                  : '● Offline'}
            </strong>
          </div>

          <div className="info-row">
            <span>VPN Server</span>
            <strong
              className={
                vpnOnline
                  ? 'server-online'
                  : 'server-offline'
              }
            >
              {statusLoading
                ? 'Checking...'
                : vpnOnline
                  ? '● Online'
                  : '● Offline'}
            </strong>
          </div>

          <div className="info-row">
            <span>PostgreSQL</span>
            <strong
              className={
                databaseOnline
                  ? 'server-online'
                  : 'server-offline'
              }
            >
              {statusLoading
                ? 'Checking...'
                : databaseOnline
                  ? '● Online'
                  : '● Offline'}
            </strong>
          </div>

          <div className="info-row">
            <span>VPN Port</span>
            <strong>5555</strong>
          </div>

          <div className="info-row">
            <span>API Port</span>
            <strong>8080</strong>
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
            <span>
              {databaseOnline
                ? 'PostgreSQL connected'
                : 'PostgreSQL unavailable'}
            </span>
          </div>

          <div className="activity-item">
            <span>Current session</span>
            <span>
              {connected ? 'Connected' : 'Inactive'}
            </span>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
