import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI SRE Platform | Autonomous Operations",
  description: "Enterprise-grade AI-powered Site Reliability Engineering dashboard.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <div className="app-container">
          <aside className="sidebar">
            <div className="logo-container">
              <div className="logo-icon"></div>
              <span className="logo-text">AI <span className="text-gradient">SRE</span></span>
            </div>
            
            <nav className="nav-menu">
              <a href="/" className="nav-item active">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                Dashboard
              </a>
              <a href="/incidents" className="nav-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                Incidents
              </a>
              <a href="/slo" className="nav-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"></path></svg>
                SLOs & Budgets
              </a>
              <a href="/healing" className="nav-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 9.36l-7.53 7.53a1 1 0 0 1-1.42 0l-1.41-1.41a1 1 0 0 1 0-1.42l7.53-7.53a6 6 0 0 1 9.36-7.94l-3.77 3.77a1 1 0 0 0 0 1.41z"></path></svg>
                Self-Healing
              </a>
            </nav>

            <div className="sidebar-footer">
              <div className="user-profile">
                <div className="avatar">JD</div>
                <div className="user-info">
                  <span className="user-name">John Doe</span>
                  <span className="user-role badge info" style={{fontSize: '0.6rem'}}>SRE LEAD</span>
                </div>
              </div>
            </div>
          </aside>

          <main className="main-content">
            <header className="topbar">
              <div className="search-bar">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" strokeWidth="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                <input type="text" placeholder="Search services, traces, incidents..." />
              </div>
              <div className="topbar-actions">
                <button className="icon-btn">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                  <span className="notification-dot"></span>
                </button>
              </div>
            </header>
            
            <div className="page-content">
              {children}
            </div>
          </main>
        </div>

        <style dangerouslySetInnerHTML={{__html: `
          .app-container {
            display: flex;
            height: 100vh;
            overflow: hidden;
          }
          
          .sidebar {
            width: 260px;
            background: var(--bg-surface);
            border-right: 1px solid var(--border-subtle);
            display: flex;
            flex-direction: column;
            padding: 24px 0;
            z-index: 10;
          }
          
          .logo-container {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 0 24px 32px;
          }
          
          .logo-icon {
            width: 32px;
            height: 32px;
            background: var(--primary-gradient);
            border-radius: 8px;
            box-shadow: var(--shadow-glow);
          }
          
          .logo-text {
            font-family: var(--font-display);
            font-size: 1.5rem;
            font-weight: 800;
          }
          
          .nav-menu {
            display: flex;
            flex-direction: column;
            gap: 4px;
            padding: 0 12px;
            flex: 1;
          }
          
          .nav-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            border-radius: 8px;
            color: var(--text-muted);
            font-weight: 500;
            transition: all var(--transition-fast);
          }
          
          .nav-item:hover {
            color: var(--text-main);
            background: rgba(255, 255, 255, 0.05);
          }
          
          .nav-item.active {
            color: var(--text-main);
            background: rgba(138, 99, 210, 0.15);
            border-left: 3px solid var(--primary);
          }
          
          .sidebar-footer {
            padding: 24px 24px 0;
            border-top: 1px solid var(--border-subtle);
          }
          
          .user-profile {
            display: flex;
            align-items: center;
            gap: 12px;
          }
          
          .avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: var(--secondary-gradient);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 0.9rem;
            color: var(--bg-base);
          }
          
          .user-info {
            display: flex;
            flex-direction: column;
          }
          
          .user-name {
            font-size: 0.9rem;
            font-weight: 600;
          }
          
          .main-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            position: relative;
          }
          
          .topbar {
            height: 72px;
            border-bottom: 1px solid var(--border-subtle);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 32px;
            background: rgba(11, 14, 20, 0.8);
            backdrop-filter: blur(12px);
            z-index: 5;
          }
          
          .search-bar {
            display: flex;
            align-items: center;
            gap: 12px;
            background: var(--bg-surface-elevated);
            padding: 8px 16px;
            border-radius: 20px;
            width: 300px;
            border: 1px solid var(--border-subtle);
            transition: all var(--transition-fast);
          }
          
          .search-bar:focus-within {
            border-color: var(--primary);
            box-shadow: 0 0 0 2px rgba(111, 66, 193, 0.2);
          }
          
          .search-bar input {
            background: transparent;
            border: none;
            color: var(--text-main);
            outline: none;
            width: 100%;
            font-family: var(--font-sans);
          }
          
          .icon-btn {
            background: transparent;
            border: none;
            color: var(--text-muted);
            cursor: pointer;
            position: relative;
            padding: 8px;
            border-radius: 50%;
            transition: all var(--transition-fast);
          }
          
          .icon-btn:hover {
            color: var(--text-main);
            background: var(--bg-surface-elevated);
          }
          
          .notification-dot {
            position: absolute;
            top: 6px;
            right: 8px;
            width: 8px;
            height: 8px;
            background: var(--status-critical);
            border-radius: 50%;
            border: 2px solid var(--bg-base);
          }
          
          .page-content {
            flex: 1;
            overflow-y: auto;
            padding: 32px;
          }
        `}} />
      </body>
    </html>
  );
}
