import { useState } from 'react';
import { Code2, Mail, Lock, User, AtSign, Github, Chrome, ArrowRight, Check, Sparkles, Zap } from 'lucide-react';
import { CosmicStars } from "../components/workspace/CosmicStars";

import { useNavigate } from 'react-router-dom';

export default function Signup() {
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    language: '',
    acceptTerms: false,
  });
  const [focusedField, setFocusedField] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleChange = (field: string, value: string | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <div className="min-h-screen bg-[#06070F] text-white relative overflow-hidden flex items-center justify-center py-12">
      {/* Cosmic Background */}
      <CosmicStars />

      {/* Background Gradients */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-1/4 right-1/4 w-[500px] h-[500px] bg-[#6B54FF] rounded-full blur-[150px] opacity-20" />
        <div className="absolute bottom-1/3 left-1/4 w-[600px] h-[600px] bg-[#00CFFF] rounded-full blur-[150px] opacity-15 animate-pulse" />
        <div className="absolute top-1/2 right-1/3 w-[400px] h-[400px] bg-[#D56BFF] rounded-full blur-[150px] opacity-10" />
      </div>

      {/* Floating Particles */}
      <div className="fixed inset-0 pointer-events-none">
        {Array.from({ length: 60 }).map((_, i) => (
          <div
            key={i}
            className="absolute bg-white rounded-full"
            style={{
              width: `${Math.random() * 3 + 1}px`,
              height: `${Math.random() * 3 + 1}px`,
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animation: `float-particle ${Math.random() * 15 + 10}s linear infinite`,
              animationDelay: `${Math.random() * 5}s`,
              opacity: Math.random() * 0.5 + 0.2,
            }}
          />
        ))}
      </div>

      <style>{`
        @keyframes float-particle {
          0%, 100% { transform: translateY(0px) translateX(0px) scale(1); opacity: 0.2; }
          50% { transform: translateY(-150px) translateX(80px) scale(1.2); opacity: 0.6; }
        }
        @keyframes galaxy-swirl {
          0% { transform: rotate(0deg) scale(1); }
          50% { transform: rotate(180deg) scale(1.1); }
          100% { transform: rotate(360deg) scale(1); }
        }
      `}</style>

      {/* Main Content */}
      <div className="relative z-10 w-full max-w-7xl mx-auto px-8 flex items-center justify-between gap-16">
        {/* Left Side - Visual Illustration */}
        <div className="flex-1 space-y-8">
          <div className="relative w-full h-96 flex items-center justify-center">
            <div className="absolute inset-0 opacity-30" style={{ animation: 'galaxy-swirl 30s linear infinite' }}>
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 rounded-full bg-gradient-to-br from-[#6B54FF]/30 via-[#00CFFF]/20 to-transparent blur-2xl" />
            </div>

            <div className="relative z-10">
              <div className="w-48 h-48 rounded-full bg-gradient-to-br from-[#6B54FF] to-[#00CFFF] flex items-center justify-center relative">
                <Code2 className="w-24 h-24" />
                <div className="absolute inset-0 rounded-full bg-gradient-to-br from-[#6B54FF] to-[#00CFFF] blur-2xl opacity-50 animate-pulse" />
              </div>

              <div className="absolute inset-0 pointer-events-none">
                <div className="relative w-full h-full" style={{ animation: 'galaxy-swirl 20s linear infinite' }}>
                  {[
                    { icon: Sparkles, color: '#6B54FF', angle: 0 },
                    { icon: Zap, color: '#00CFFF', angle: 120 },
                    { icon: Code2, color: '#D56BFF', angle: 240 },
                  ].map((item, idx) => {
                    const Icon = item.icon;
                    return (
                      <div
                        key={idx}
                        className="absolute top-1/2 left-1/2 w-12 h-12 rounded-xl flex items-center justify-center"
                        style={{
                          transform: `rotate(${item.angle}deg) translateY(-120px) rotate(-${item.angle}deg)`,
                          backgroundColor: `${item.color}20`,
                          border: `2px solid ${item.color}`,
                          boxShadow: `0 0 20px ${item.color}60`,
                        }}
                      >
                        <Icon className="w-6 h-6" style={{ color: item.color }} />
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-4 max-w-xl">
            <h1 className="text-6xl font-bold leading-tight">
              Welcome to Your
              <br />
              <span className="bg-gradient-to-r from-[#6B54FF] via-[#00CFFF] to-[#D56BFF] bg-clip-text text-transparent">
                Coding Universe
              </span>
            </h1>
            <p className="text-xl text-[#C9D4F0]">
              Start your journey with CodeAstra and collaborate with developers worldwide. 🚀
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            {[{ icon: Code2, text: 'Real-time Collaboration' }, { icon: Zap, text: 'Lightning Fast' }, { icon: Sparkles, text: 'AI-Powered' }].map((feature, idx) => {
              const Icon = feature.icon;
              return (
                <div
                  key={idx}
                  className="flex items-center gap-2 bg-white/5 border border-white/10 rounded-full px-4 py-2 backdrop-blur-sm"
                >
                  <Icon className="w-4 h-4 text-[#00CFFF]" />
                  <span className="text-sm text-[#C9D4F0]">{feature.text}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right Side - Signup Form */}
        <div className="w-full max-w-md">
          <div className="bg-[#0A0E1A]/40 backdrop-blur-xl border border-white/10 rounded-3xl p-8 shadow-2xl">
            <div className="mb-6">
              <h2 className="text-3xl font-bold mb-2">Create Your Account</h2>
              <p className="text-[#8892A6]">Join CodeAstra today!</p>
            </div>

            <form className="space-y-4">
              {/* ...input fields unchanged... */}
              {/* Terms Checkbox */}
              <div className="flex items-start gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => handleChange('acceptTerms', !formData.acceptTerms)}
                  className={`w-5 h-5 rounded border-2 flex items-center justify-center transition-all mt-0.5 ${
                    formData.acceptTerms
                      ? 'bg-[#6B54FF] border-[#6B54FF] shadow-[0_0_10px_rgba(107,84,255,0.5)]'
                      : 'border-white/30'
                  }`}
                >
                  {formData.acceptTerms && <Check className="w-3 h-3" />}
                </button>
                <label className="text-sm text-[#C9D4F0]">
                  I accept the{' '}
                  <button
                    type="button"
                    className="text-[#00CFFF] hover:text-[#6B54FF] transition-colors underline"
                  >
                    Terms of Service
                  </button>{' '}
                  and{' '}
                  <button
                    type="button"
                    className="text-[#00CFFF] hover:text-[#6B54FF] transition-colors underline"
                  >
                    Privacy Policy
                  </button>
                </label>
              </div>

              <button
                type="submit"
                className="w-full bg-gradient-to-r from-[#6B54FF] to-[#00CFFF] text-white font-semibold py-3 px-6 rounded-xl hover:shadow-[0_0_30px_rgba(107,84,255,0.5)] transition-all duration-300 flex items-center justify-center gap-2 group"
              >
                <span>Create Account</span>
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </button>
            </form>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-white/10" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-[#0A0E1A] text-[#8892A6]">or sign up with</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <button className="bg-white/5 border border-white/10 hover:border-[#6B54FF]/50 hover:bg-white/10 text-white py-3 px-4 rounded-xl transition-all flex items-center justify-center gap-2">
                <Github className="w-5 h-5" />
                <span>GitHub</span>
              </button>
              <button className="bg-white/5 border border-white/10 hover:border-[#00CFFF]/50 hover:bg-white/10 text-white py-3 px-4 rounded-xl transition-all flex items-center justify-center gap-2">
                <Chrome className="w-5 h-5" />
                <span>Google</span>
              </button>
            </div>

            {/* ✅ Fixed Login Link */}
            <div className="mt-6 text-center">
              <p className="text-[#8892A6]">
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={() => navigate('/login')}
                  className="text-[#00CFFF] hover:text-[#6B54FF] font-semibold transition-colors"
                >
                  Login
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
