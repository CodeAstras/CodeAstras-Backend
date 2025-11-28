import { useState } from 'react';
import { Code2, Mail, Lock, Github, Chrome, ArrowRight, Sparkles, Terminal } from 'lucide-react';
import { CosmicStars } from '../components/workspace/CosmicStars';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [focusedField, setFocusedField] = useState<string | null>(null);

  return (
    <div className="min-h-screen bg-[#06070F] text-white relative overflow-hidden flex items-center justify-center">
      {/* Cosmic Background */}
      <CosmicStars />

      {/* Background Gradients */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-[500px] h-[500px] bg-[#6B54FF] rounded-full blur-[150px] opacity-20 animate-pulse" />
        <div className="absolute bottom-1/3 right-1/4 w-[600px] h-[600px] bg-[#00CFFF] rounded-full blur-[150px] opacity-15" />
        <div className="absolute top-1/2 left-1/2 w-[400px] h-[400px] bg-[#D56BFF] rounded-full blur-[150px] opacity-10" />
      </div>

      {/* Floating Particles */}
      <div className="fixed inset-0 pointer-events-none">
        {Array.from({ length: 50 }).map((_, i) => (
          <div
            key={i}
            className="absolute w-1 h-1 bg-white rounded-full opacity-30"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animation: `float ${Math.random() * 10 + 10}s linear infinite`,
              animationDelay: `${Math.random() * 5}s`,
            }}
          />
        ))}
      </div>

      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0px) translateX(0px); opacity: 0.3; }
          50% { transform: translateY(-100px) translateX(50px); opacity: 0.6; }
        }
        @keyframes glow-pulse {
          0%, 100% { opacity: 0.5; }
          50% { opacity: 1; }
        }
        @keyframes rotate-slow {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>

      {/* Main Content */}
      <div className="relative z-10 w-full max-w-7xl mx-auto px-8 flex items-center justify-between gap-16">
        {/* Left Side - Visual Area */}
        <div className="flex-1 space-y-8">
          {/* Logo with Glow */}
          <div className="relative inline-block">
            <div className="relative">
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-[#6B54FF] to-[#00CFFF] flex items-center justify-center relative z-10">
                <Code2 className="w-12 h-12" />
              </div>
              <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-[#6B54FF] to-[#00CFFF] blur-2xl opacity-60 animate-pulse" />
            </div>
            
            {/* Rotating Glyphs */}
            <div className="absolute -inset-4 pointer-events-none">
              <div className="relative w-full h-full" style={{ animation: 'rotate-slow 20s linear infinite' }}>
                {[0, 60, 120, 180, 240, 300].map((angle) => (
                  <div
                    key={angle}
                    className="absolute w-2 h-2 bg-[#00CFFF] rounded-full"
                    style={{
                      top: '50%',
                      left: '50%',
                      transform: `rotate(${angle}deg) translateY(-50px)`,
                      boxShadow: '0 0 10px #00CFFF',
                    }}
                  />
                ))}
              </div>
            </div>
          </div>

          {/* Headline */}
          <div className="space-y-4 max-w-xl">
            <h1 className="text-6xl font-bold leading-tight">
              Code Together.
              <br />
              <span className="bg-gradient-to-r from-[#6B54FF] via-[#00CFFF] to-[#D56BFF] bg-clip-text text-transparent">
                From Anywhere.
              </span>
            </h1>
            <p className="text-xl text-[#C9D4F0]">
              Join the next-generation collaborative coding platform.
            </p>
          </div>

          {/* Constellation Lines */}
          <div className="relative h-48 opacity-40">
            <svg className="w-full h-full" viewBox="0 0 400 200">
              {/* Connection lines */}
              <line x1="50" y1="50" x2="150" y2="100" stroke="#6B54FF" strokeWidth="1" opacity="0.5" />
              <line x1="150" y1="100" x2="250" y2="80" stroke="#00CFFF" strokeWidth="1" opacity="0.5" />
              <line x1="250" y1="80" x2="350" y2="120" stroke="#D56BFF" strokeWidth="1" opacity="0.5" />
              <line x1="150" y1="100" x2="200" y2="150" stroke="#6B54FF" strokeWidth="1" opacity="0.5" />
              
              {/* Nodes */}
              <circle cx="50" cy="50" r="4" fill="#6B54FF" style={{ filter: 'drop-shadow(0 0 6px #6B54FF)' }} />
              <circle cx="150" cy="100" r="4" fill="#00CFFF" style={{ filter: 'drop-shadow(0 0 6px #00CFFF)' }} />
              <circle cx="250" cy="80" r="4" fill="#D56BFF" style={{ filter: 'drop-shadow(0 0 6px #D56BFF)' }} />
              <circle cx="350" cy="120" r="4" fill="#6B54FF" style={{ filter: 'drop-shadow(0 0 6px #6B54FF)' }} />
              <circle cx="200" cy="150" r="4" fill="#00CFFF" style={{ filter: 'drop-shadow(0 0 6px #00CFFF)' }} />
            </svg>
          </div>

          {/* Terminal-style Message */}
          <div className="bg-[#0A0E1A]/60 backdrop-blur-md border border-[#6B54FF]/20 rounded-xl p-4 font-mono text-sm">
            <div className="flex items-center gap-2 text-[#00CFFF] mb-2">
              <Terminal className="w-4 h-4" />
              <span>$ codeastra --welcome</span>
            </div>
            <div className="text-[#8892A6]">
              {'>'} Initializing collaborative workspace...
              <br />
              {'>'} Loading real-time sync engine...
              <br />
              <span className="text-[#00CFFF]">{'>'} Ready to build together! 🚀</span>
            </div>
          </div>
        </div>

        {/* Right Side - Login Form */}
        <div className="w-full max-w-md">
          <div className="bg-[#0A0E1A]/40 backdrop-blur-xl border border-white/10 rounded-3xl p-8 shadow-2xl">
            {/* Header */}
            <div className="mb-8">
              <h2 className="text-3xl font-bold mb-2">Login to CodeAstra</h2>
              <p className="text-[#8892A6]">Welcome back, developer! 👋</p>
            </div>

            {/* Form */}
            <form className="space-y-6">
              {/* Email Input */}
              <div>
                <label className="block text-sm font-medium text-[#C9D4F0] mb-2">Email</label>
                <div className="relative">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#8892A6]" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    onFocus={() => setFocusedField('email')}
                    onBlur={() => setFocusedField(null)}
                    placeholder="your.email@example.com"
                    className={`w-full bg-white/5 border rounded-xl pl-12 pr-4 py-3 text-white placeholder-[#8892A6] focus:outline-none transition-all ${
                      focusedField === 'email'
                        ? 'border-[#6B54FF] shadow-[0_0_20px_rgba(107,84,255,0.3)]'
                        : 'border-white/10'
                    }`}
                  />
                  {focusedField === 'email' && (
                    <div className="absolute inset-0 rounded-xl border-2 border-[#6B54FF]/50 pointer-events-none animate-pulse" />
                  )}
                </div>
              </div>

              {/* Password Input */}
              <div>
                <label className="block text-sm font-medium text-[#C9D4F0] mb-2">Password</label>
                <div className="relative">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#8892A6]" />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onFocus={() => setFocusedField('password')}
                    onBlur={() => setFocusedField(null)}
                    placeholder="••••••••"
                    className={`w-full bg-white/5 border rounded-xl pl-12 pr-4 py-3 text-white placeholder-[#8892A6] focus:outline-none transition-all ${
                      focusedField === 'password'
                        ? 'border-[#6B54FF] shadow-[0_0_20px_rgba(107,84,255,0.3)]'
                        : 'border-white/10'
                    }`}
                  />
                  {focusedField === 'password' && (
                    <div className="absolute inset-0 rounded-xl border-2 border-[#6B54FF]/50 pointer-events-none animate-pulse" />
                  )}
                </div>
              </div>

              {/* Forgot Password */}
              <div className="flex justify-end">
                <a href="#" className="text-sm text-[#00CFFF] hover:text-[#6B54FF] transition-colors">
                  Forgot Password?
                </a>
              </div>

              {/* Login Button */}
              <button
                type="submit"
                className="w-full bg-gradient-to-r from-[#6B54FF] to-[#00CFFF] text-white font-semibold py-3 px-6 rounded-xl hover:shadow-[0_0_30px_rgba(107,84,255,0.5)] transition-all duration-300 flex items-center justify-center gap-2 group"
              >
                <span>Login</span>
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </button>
            </form>

            {/* Divider */}
            <div className="relative my-8">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-white/10" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-[#0A0E1A] text-[#8892A6]">or continue with</span>
              </div>
            </div>

            {/* Social Login Buttons */}
            <div className="space-y-3">
              <button className="w-full bg-white/5 border border-white/10 hover:border-[#6B54FF]/50 hover:bg-white/10 text-white py-3 px-4 rounded-xl transition-all flex items-center justify-center gap-3 group">
                <Github className="w-5 h-5" />
                <span>Continue with GitHub</span>
              </button>
              <button className="w-full bg-white/5 border border-white/10 hover:border-[#00CFFF]/50 hover:bg-white/10 text-white py-3 px-4 rounded-xl transition-all flex items-center justify-center gap-3 group">
                <Chrome className="w-5 h-5" />
                <span>Continue with Google</span>
              </button>
            </div>

            {/* Sign Up Link */}
            <div className="mt-8 text-center">
              <p className="text-[#8892A6]">
                Don't have an account?{' '}
                <a href="#/signup" className="text-[#00CFFF] hover:text-[#6B54FF] font-semibold transition-colors">
                  Sign Up
                </a>
              </p>
            </div>
          </div>

          {/* Decorative Elements */}
          <div className="absolute -right-20 top-1/2 w-40 h-40 bg-[#6B54FF] rounded-full blur-[100px] opacity-20 pointer-events-none" />
        </div>
      </div>
    </div>
  );
}