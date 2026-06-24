import { SidebarNav } from "@/shared/ui/SidebarNav";

export default function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
        <div className="app-container">
          <aside className="sidebar">
            <div className="logo-container">
              <div className="logo-icon"></div>
              <span className="logo-text">AI SRE</span>
            </div>
            
            <SidebarNav />

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
  );
}
