import { motion, useScroll, useMotionValueEvent } from "motion/react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

export function Header() {

    const navigate = useNavigate();

    const navItems = [
        {
            label: "Pricing",
            action: () => document.getElementById('pricing')?.scrollIntoView({ behavior: 'smooth' })
        },
        {
            label: "About",
            action: () => {}
        },
        {
            label: "Dashboard",
            action: () => navigate('/dashboard')
        },
        {
            label: "Login",
            action: () => navigate('/login'),
            primary: true
        }
    ];

    const [hidden, setHidden] = useState(false);
    const { scrollY } = useScroll();



  useMotionValueEvent(scrollY, "change", (latest) => {
    const previous = scrollY.getPrevious();
    if (latest > previous && latest > 150) {
      setHidden(true);
    } else {
      setHidden(false);
    }
  });

  return (
    <motion.header
      variants={{
        visible: { y: 0 },
        hidden: { y: "-100%" },
      }}
      animate={hidden ? "hidden" : "visible"}
      transition={{ duration: 0.35, ease: "easeInOut" }}
      className="fixed top-0 left-0 right-0 z-50 px-4 md:px-8 py-4 md:py-6"
    >
      <div className="max-w-[1600px] mx-auto flex items-center justify-between gap-4">
        {/* Left Logo */}
        <motion.div
          className="relative"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <div className="relative px-6 md:px-8 py-2 md:py-3 rounded-xl md:rounded-2xl backdrop-blur-xl bg-gradient-to-br from-white/10 to-white/5 border border-white/20 shadow-2xl">
            {/* Subtle neon edge glow - reduced */}
            <div className="absolute inset-0 rounded-xl md:rounded-2xl bg-gradient-to-br from-cyan-400/10 via-purple-500/10 to-cyan-400/10 blur-lg opacity-30" />
            
            <h1 className="relative z-10 text-2xl md:text-3xl tracking-wider bg-gradient-to-r from-cyan-400 via-purple-400 to-cyan-400 bg-clip-text text-transparent whitespace-nowrap">
              CodeAstra
            </h1>
          </div>
        </motion.div>

        {/* Right Navigation */}
        <nav className="flex items-center gap-1 md:gap-2">
          {navItems.map((item, index) => (
            <motion.button
              key={item.label}
              onClick={item.action}
              className={`relative px-3 md:px-6 py-2 md:py-2.5 rounded-full text-xs md:text-sm backdrop-blur-xl border transition-all duration-300 group overflow-hidden ${
                item.primary
                  ? 'bg-gradient-to-r from-purple-500 to-cyan-500 border-transparent hover:shadow-lg hover:shadow-cyan-500/50'
                  : 'bg-white/5 border-white/10 hover:border-cyan-400/50 hover:bg-white/10'
              }`}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 + 0.4 }}
            >
              {/* Neon glow effect on hover */}
              {!item.primary && (
                <div className="absolute inset-0 rounded-full bg-gradient-to-r from-cyan-400/0 via-cyan-400/20 to-cyan-400/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
              )}
              <span className="relative z-10">{item.label}</span>
            </motion.button>
          ))}
        </nav>
      </div>
    </motion.header>
  );
}