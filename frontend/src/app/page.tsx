import Link from 'next/link';
import Image from 'next/image';

export default function LandingPage() {
  return (
    <div className="landing-container">
      {/* ----------------- NAVBAR ----------------- */}
      <nav className="landing-nav">
        <div className="nav-logo">
          <span className="logo-text">AI SRE</span>
        </div>
        <div className="nav-actions">
          <Link href="/dashboard" className="btn-nav">
            Dashboard
          </Link>
        </div>
      </nav>

      {/* ----------------- HERO SECTION ----------------- */}
      <section className="landing-hero">
        <div className="hero-container">
          <div className="hero-content">
            <div className="system-status-badge">
              <span className="pulse-ring"></span>
              <span className="status-text">SYSTEM ONLINE</span>
            </div>
            <h1 className="hero-title">
              Autonomous <br /> <span className="text-primary">AI Operations</span>
            </h1>
            <p className="hero-subtitle">
              The ultimate FAANG-tier SRE platform. Experience zero-touch incident resolution, predictive SLO tracking, and real-time autonomous healing without human intervention.
            </p>
            <div className="hero-actions">
              <Link href="/dashboard" className="btn btn-primary launch-btn">
                Launch Platform
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
              </Link>
            </div>
          </div>
          <div className="hero-image-wrapper">
            <div className="hero-image-glow"></div>
            <Image 
              src="/images/robot-ui.png" 
              alt="AI Robot UI" 
              width={650} 
              height={650} 
              className="hero-robot-img"
              priority 
            />
          </div>
        </div>
      </section>

      {/* ----------------- CORE CAPABILITIES ----------------- */}
      <section className="landing-section">
        <div className="section-header">
          <h2>Core Capabilities</h2>
          <p>Next-generation reliability engineering powered by Large Language Models.</p>
        </div>
        <div className="features-grid">
          <div className="glass-panel feature-card">
            <div className="feature-image-wrapper">
              <Image src="/images/auto_remediation.png" alt="Auto Remediation" fill className="feature-image" />
            </div>
            <h3>Auto-Remediation</h3>
            <p>AI-driven playbooks automatically execute to self-heal critical infrastructure incidents in real-time before customers notice.</p>
          </div>

          <div className="glass-panel feature-card">
            <div className="feature-image-wrapper">
              <Image src="/images/ai_intelligence.png" alt="AI Intelligence" fill className="feature-image" />
            </div>
            <h3>AI Incident Intelligence</h3>
            <p>Advanced LLMs analyze traces, logs, and metrics instantly to provide root-cause analysis with actionable resolution steps.</p>
          </div>

          <div className="glass-panel feature-card">
            <div className="feature-image-wrapper">
              <Image src="/images/predictive_slos.png" alt="Predictive SLOs" fill className="feature-image" />
            </div>
            <h3>Predictive SLOs</h3>
            <p>Multi-window burn rate tracking combined with predictive forecasting ensures error budgets are strictly maintained.</p>
          </div>
        </div>
      </section>

      {/* ----------------- HOW IT WORKS ----------------- */}
      <section className="landing-section alternate">
        <div className="section-header">
          <h2>How It Works</h2>
          <p>A completely autonomous lifecycle from detection to resolution.</p>
        </div>
        <div className="workflow-container">
          <div className="workflow-step">
            <div className="step-number">01</div>
            <h4>Ingest Telemetry</h4>
            <p>Continuously aggregates logs, metrics, and traces from your entire microservice fleet via our high-throughput pipeline.</p>
          </div>
          <div className="workflow-connector"></div>
          <div className="workflow-step">
            <div className="step-number">02</div>
            <h4>AI Analysis</h4>
            <p>The reasoning engine identifies anomalies, correlates events, and synthesizes a precise root cause analysis in seconds.</p>
          </div>
          <div className="workflow-connector"></div>
          <div className="workflow-step">
            <div className="step-number">03</div>
            <h4>Autonomous Execution</h4>
            <p>Self-healing agents apply guardrailed runbooks (e.g., rolling back deployments, scaling pods) to instantly resolve the issue.</p>
          </div>
        </div>
      </section>

      {/* ----------------- ADVANTAGES ----------------- */}
      <section className="landing-section">
        <div className="section-header">
          <h2>The Advantage</h2>
          <p>Why leading engineering teams choose Autonomous AI Operations.</p>
        </div>
        <div className="advantages-grid">
          <div className="advantage-item">
            <div className="advantage-img-wrapper">
              <Image src="/images/zero_touch.png" alt="Zero-Touch" width={80} height={80} className="advantage-img" />
            </div>
            <div>
              <h4>Zero-Touch Operations</h4>
              <p>Eliminate manual paging and midnight firefighting. The system handles P1 incidents autonomously.</p>
            </div>
          </div>
          <div className="advantage-item">
            <div className="advantage-img-wrapper">
              <Image src="/images/error_budget.png" alt="Error Budgets" width={80} height={80} className="advantage-img" />
            </div>
            <div>
              <h4>Protect Error Budgets</h4>
              <p>Stop relying on reactive alerts. Predictive burn-rate tracking catches degradation before SLOs are breached.</p>
            </div>
          </div>
          <div className="advantage-item">
            <div className="advantage-img-wrapper">
              <Image src="/images/infinite_scalability.png" alt="Scalability" width={80} height={80} className="advantage-img" />
            </div>
            <div>
              <h4>Also Scalable</h4>
              <p>The AI Engine effortlessly tracks thousands of microservices simultaneously, scaling your SRE capabilities without adding headcount.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ----------------- FOOTER ----------------- */}
      <footer className="landing-footer">
        <div className="footer-content">
          <div className="footer-logo">AI SRE</div>
          <p className="footer-copy">&copy; {new Date().getFullYear()} Autonomous AI Operations. All rights reserved.</p>
        </div>
      </footer>

      <style dangerouslySetInnerHTML={{
        __html: `
        .landing-container {
          width: 100%;
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 0;
          overflow-x: hidden;
          background: #000;
        }

        /* ---------------- NAVBAR ---------------- */
        .landing-nav {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          height: 80px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 4rem;
          background: rgba(0, 0, 0, 0.4);
          backdrop-filter: blur(20px);
          -webkit-backdrop-filter: blur(20px);
          border-bottom: 1px solid rgba(0, 229, 255, 0.1);
          z-index: 1000;
        }

        .nav-logo {
          font-family: 'Outfit', sans-serif;
          font-size: 1.8rem;
          font-weight: 900;
          letter-spacing: -1px;
          color: rgba(255, 255, 255, 0.5);
        }

        .btn-nav {
          padding: 10px 24px;
          font-size: 1rem;
          font-weight: 600;
          color: #fff;
          text-decoration: none;
          background: rgba(0, 229, 255, 0.1);
          border: 1px solid rgba(0, 229, 255, 0.3);
          border-radius: 50px;
          transition: all 0.3s ease;
        }

        .btn-nav:hover {
          background: rgba(0, 229, 255, 0.2);
          border-color: rgba(0, 229, 255, 0.6);
          box-shadow: 0 0 15px rgba(0, 229, 255, 0.3);
        }

        /* Ambient glowing orb behind hero */
        .landing-hero {
          position: relative;
          width: 100%;
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 8rem 2rem 2rem;
        }

        .landing-hero::before {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 1000px;
          height: 1000px;
          background: radial-gradient(circle, rgba(0, 229, 255, 0.08) 0%, transparent 60%);
          z-index: 0;
          pointer-events: none;
        }

        .hero-container {
          display: grid;
          grid-template-columns: 1.1fr 0.9fr;
          gap: 4rem;
          align-items: center;
          max-width: 1400px;
          width: 100%;
          position: relative;
          z-index: 1;
        }

        .hero-content {
          animation: slideRight 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          text-align: left;
        }

        /* Hero Image Styles */
        .hero-image-wrapper {
          position: relative;
          display: flex;
          justify-content: center;
          align-items: center;
          transition: transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
        }
        
        .hero-image-wrapper:hover {
          transform: translateY(-10px) scale(1.02);
        }

        .hero-image-glow {
          position: absolute;
          width: 90%;
          height: 90%;
          background: radial-gradient(circle, rgba(0, 229, 255, 0.3) 0%, transparent 65%);
          filter: blur(50px);
          z-index: -1;
          opacity: 0.8;
          transition: opacity 0.5s ease;
        }
        
        .hero-image-wrapper:hover .hero-image-glow {
          opacity: 1;
        }

        .hero-robot-img {
          object-fit: contain;
          max-width: 100%;
          height: auto;
          filter: drop-shadow(0 20px 50px rgba(0, 229, 255, 0.2));
          -webkit-mask-image: radial-gradient(circle, black 60%, transparent 100%);
          mask-image: radial-gradient(circle, black 60%, transparent 100%);
        }

        @keyframes slideRight {
          from { opacity: 0; transform: translateX(-40px); }
          to { opacity: 1; transform: translateX(0); }
        }

        /* Advanced System Online Badge */
        .system-status-badge {
          display: inline-flex;
          align-items: center;
          gap: 12px;
          background: rgba(0, 255, 170, 0.08);
          border: 1px solid rgba(0, 255, 170, 0.3);
          padding: 8px 20px;
          border-radius: 50px;
          margin-bottom: 2rem;
          box-shadow: 0 0 20px rgba(0, 255, 170, 0.1);
        }

        .pulse-ring {
          width: 10px;
          height: 10px;
          background: #00ffa2;
          border-radius: 50%;
          position: relative;
          box-shadow: 0 0 10px #00ffa2;
        }

        .pulse-ring::before {
          content: '';
          position: absolute;
          top: -50%; left: -50%;
          width: 200%; height: 200%;
          border: 1px solid #00ffa2;
          border-radius: 50%;
          animation: radarPulse 2s infinite cubic-bezier(0.16, 1, 0.3, 1);
        }

        @keyframes radarPulse {
          0% { transform: scale(0.5); opacity: 1; }
          100% { transform: scale(2.5); opacity: 0; }
        }

        .status-text {
          color: #00ffa2;
          font-weight: 700;
          font-size: 0.85rem;
          letter-spacing: 2px;
        }

        .hero-title {
          font-size: 4rem;
          font-weight: 800;
          line-height: 1.15;
          margin: 0 0 1.2rem 0;
          letter-spacing: -1.5px;
          color: #ffffff;
          text-shadow: 0 4px 20px rgba(0,0,0,0.5);
        }

        .text-primary {
          background: linear-gradient(90deg, #00e5ff, #0077ff);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          text-shadow: none;
        }

        .hero-subtitle {
          font-size: 1.15rem;
          color: rgba(255, 255, 255, 0.75);
          line-height: 1.6;
          margin-bottom: 2.5rem;
          max-width: 600px;
          font-weight: 400;
          letter-spacing: 0.2px;
        }

        .hero-actions {
          display: flex;
          justify-content: flex-start;
        }

        .launch-btn {
          padding: 16px 42px;
          font-size: 1.15rem;
          font-weight: 600;
          border-radius: 50px;
          display: flex;
          align-items: center;
          gap: 12px;
          text-decoration: none;
          box-shadow: 0 8px 25px rgba(0, 229, 255, 0.3);
          transition: transform 0.3s ease, box-shadow 0.3s ease;
          position: relative;
          overflow: hidden;
          background: linear-gradient(135deg, #00e5ff, #0077ff);
          color: #fff;
          border: none;
        }

        /* Shine Effect for Button */
        .launch-btn::after {
          content: '';
          position: absolute;
          top: 0;
          left: -100%;
          width: 50%;
          height: 100%;
          background: linear-gradient(to right, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0) 100%);
          transform: skewX(-25deg);
          animation: shine 4s infinite;
        }

        @keyframes shine {
          0% { left: -100%; }
          20% { left: 200%; }
          100% { left: 200%; }
        }

        .launch-btn:hover {
          transform: translateY(-3px);
          box-shadow: 0 12px 30px rgba(0, 229, 255, 0.5);
        }

        /* Reusable Section Styles */
        .landing-section {
          width: 100%;
          max-width: 1200px;
          padding: 6rem 2rem;
          display: flex;
          flex-direction: column;
          align-items: center;
        }

        .landing-section.alternate {
          max-width: 100%;
          background: linear-gradient(180deg, transparent 0%, rgba(10, 14, 23, 0.8) 50%, transparent 100%);
          border-top: 1px solid rgba(0, 229, 255, 0.05);
          border-bottom: 1px solid rgba(0, 229, 255, 0.05);
        }

        .section-header {
          text-align: center;
          margin-bottom: 5rem;
          max-width: 600px;
          display: flex;
          flex-direction: column;
          align-items: center;
        }

        .section-header h2 {
          font-family: 'Outfit', sans-serif;
          font-size: 3.8rem;
          font-weight: 900;
          color: rgba(255, 255, 255, 0.5);
          margin-bottom: 1.5rem;
          letter-spacing: -1.5px;
          text-transform: uppercase;
        }

        .section-header p {
          color: var(--text-muted);
          font-size: 1.25rem;
          line-height: 1.7;
          max-width: 500px;
        }

        /* Features Grid - Ultra Premium */
        .features-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 3rem;
          width: 100%;
          max-width: 1200px;
        }

        .feature-card {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          gap: 1.5rem;
          padding: 2.5rem;
          background: linear-gradient(145deg, rgba(15, 20, 35, 0.6) 0%, rgba(5, 8, 15, 0.9) 100%);
          border: 1px solid rgba(0, 229, 255, 0.1);
          border-radius: 20px;
          transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
          position: relative;
          overflow: hidden;
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
        }
        
        /* Neon Border Glow Effect */
        .feature-card::before {
          content: '';
          position: absolute;
          top: 0; left: 0; right: 0; height: 2px;
          background: linear-gradient(90deg, transparent, var(--primary), transparent);
          opacity: 0;
          transition: opacity 0.5s ease;
        }

        .feature-card:hover {
          transform: translateY(-12px);
          border-color: rgba(0, 229, 255, 0.3);
          box-shadow: 0 20px 50px rgba(0, 229, 255, 0.15), 0 0 40px rgba(0, 229, 255, 0.1) inset;
        }

        .feature-card:hover::before {
          opacity: 1;
        }

        .feature-image-wrapper {
          width: 100%;
          height: 220px;
          position: relative;
          border-radius: 14px;
          overflow: hidden;
          box-shadow: 0 10px 25px rgba(0, 229, 255, 0.2);
          border: 1px solid rgba(0, 229, 255, 0.2);
        }

        .feature-image {
          object-fit: cover;
          transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
        }

        .feature-card:hover .feature-image {
          transform: scale(1.08);
        }

        .feature-card h3 {
          font-size: 1.6rem;
          font-weight: 800;
          background: linear-gradient(90deg, #fff, #00e5ff);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          margin: 0;
          letter-spacing: -0.5px;
        }

        .feature-card p {
          color: var(--text-muted);
          line-height: 1.7;
          margin: 0;
          font-size: 1.05rem;
          font-weight: 400;
        }

        /* Workflow Section */
        .workflow-container {
          display: flex;
          align-items: flex-start;
          justify-content: center;
          gap: 2rem;
          max-width: 1200px;
          width: 100%;
        }

        .workflow-step {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;
          padding: 2rem;
        }

        .step-number {
          font-size: 4rem;
          font-weight: 900;
          font-family: var(--font-display);
          color: rgba(255, 255, 255, 0.05);
          line-height: 1;
          margin-bottom: 1rem;
          -webkit-text-stroke: 1px rgba(0, 229, 255, 0.3);
        }

        .workflow-step h4 {
          font-size: 1.3rem;
          color: #fff;
          margin-bottom: 1rem;
        }

        .workflow-step p {
          color: var(--text-muted);
          line-height: 1.6;
        }

        .workflow-connector {
          width: 100px;
          height: 2px;
          background: linear-gradient(90deg, transparent, var(--primary), transparent);
          margin-top: 5rem;
          opacity: 0.5;
        }

        /* Advantages Section - Ultra Premium */
        .advantages-grid {
          display: flex;
          flex-direction: column;
          gap: 2.5rem;
          max-width: 850px;
          width: 100%;
        }

        .advantage-item {
          display: flex;
          align-items: center;
          gap: 2.5rem;
          padding: 2.5rem;
          background: linear-gradient(90deg, rgba(15, 20, 35, 0.6) 0%, rgba(5, 8, 15, 0.8) 100%);
          border: 1px solid rgba(0, 229, 255, 0.05);
          border-left: 3px solid transparent;
          border-radius: 24px;
          transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
          position: relative;
          overflow: hidden;
          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
        }

        .advantage-item:hover {
          background: linear-gradient(90deg, rgba(15, 20, 35, 0.9) 0%, rgba(5, 8, 15, 0.95) 100%);
          border-color: rgba(0, 229, 255, 0.2);
          border-left: 3px solid var(--primary);
          transform: translateX(12px) scale(1.01);
          box-shadow: -10px 20px 40px rgba(0, 229, 255, 0.1);
        }

        .advantage-img-wrapper {
          flex-shrink: 0;
          border-radius: 18px;
          overflow: hidden;
          box-shadow: 0 10px 25px rgba(0, 229, 255, 0.3);
          border: 2px solid rgba(0, 229, 255, 0.4);
          display: flex;
          position: relative;
        }
        
        .advantage-img-wrapper::after {
          content: '';
          position: absolute;
          top: 0; left: 0; right: 0; bottom: 0;
          box-shadow: inset 0 0 15px rgba(0, 229, 255, 0.5);
          pointer-events: none;
        }

        .advantage-img {
          object-fit: cover;
          transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
        }
        
        .advantage-item:hover .advantage-img {
          transform: scale(1.15) rotate(2deg);
        }

        .advantage-item h4 {
          font-size: 1.6rem;
          font-weight: 800;
          color: #fff;
          margin: 0 0 0.8rem 0;
          letter-spacing: -0.5px;
          transition: color 0.3s ease;
        }
        
        .advantage-item:hover h4 {
          color: var(--primary);
          text-shadow: 0 0 20px rgba(0, 229, 255, 0.4);
        }

        .advantage-item p {
          margin: 0;
          color: var(--text-muted);
          line-height: 1.7;
          font-size: 1.1rem;
        }

        @keyframes fadeUp {
          from { opacity: 0; transform: translateY(30px); }
          to { opacity: 1; transform: translateY(0); }
        }

        /* ---------------- FOOTER ---------------- */
        .landing-footer {
          width: 100%;
          padding: 4rem 2rem;
          border-top: 1px solid rgba(255, 255, 255, 0.05);
          background: rgba(5, 8, 15, 0.8);
          display: flex;
          justify-content: center;
        }

        .footer-content {
          max-width: 1200px;
          width: 100%;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .footer-logo {
          font-family: 'Outfit', sans-serif;
          font-size: 1.5rem;
          font-weight: 800;
          color: rgba(255, 255, 255, 0.5);
        }

        .footer-copy {
          color: rgba(255, 255, 255, 0.3);
          font-size: 0.95rem;
        }

        @media (max-width: 1024px) {
          .landing-nav {
            padding: 0 2rem;
          }
          .footer-content {
            flex-direction: column;
            gap: 1rem;
            text-align: center;
          }
          .hero-container {
            grid-template-columns: 1fr;
            text-align: center;
          }
          .hero-content {
            align-items: center;
            text-align: center;
          }
          .hero-actions {
            justify-content: center;
          }
          .hero-subtitle {
            max-width: 800px;
          }
          .features-grid {
            grid-template-columns: 1fr;
          }
          .workflow-container {
            flex-direction: column;
            align-items: center;
          }
          .workflow-connector {
            width: 2px;
            height: 50px;
            margin-top: 0;
            background: linear-gradient(180deg, transparent, var(--primary), transparent);
          }
          .hero-title {
            font-size: 4rem;
          }
        }
      `}} />
    </div>
  );
}
