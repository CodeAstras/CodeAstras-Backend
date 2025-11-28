import { useEffect, useRef } from 'react';

interface Star {
  id: number;
  x: number;
  y: number;
  size: number;
  opacity: number;
  speed: number;
}

export function CosmicStars() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const starsRef = useRef<Star[]>([]);
  const mousePos = useRef({ x: 0, y: 0 });
  const animationFrameId = useRef<number | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Set canvas size
    const updateSize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };
    updateSize();
    window.addEventListener('resize', updateSize);

    // Generate stars
    const generateStars = () => {
      const newStars: Star[] = [];
      const starCount = 100;

      for (let i = 0; i < starCount; i++) {
        newStars.push({
          id: i,
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          size: Math.random() * 2 + 0.5,
          opacity: Math.random() * 0.5 + 0.3,
          speed: Math.random() * 0.2 + 0.1,
        });
      }
      starsRef.current = newStars;
    };
    generateStars();

    // Mouse move handler
    const handleMouseMove = (e: MouseEvent) => {
      mousePos.current = { x: e.clientX, y: e.clientY };
    };
    window.addEventListener('mousemove', handleMouseMove);

    // Animation loop
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      starsRef.current.forEach((star) => {
        // Calculate distance to mouse
        const dx = mousePos.current.x - star.x;
        const dy = mousePos.current.y - star.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        const maxDistance = 200;

        // Move star away from mouse
        if (distance < maxDistance && distance !== 0) {
          const force = (maxDistance - distance) / maxDistance;
          star.x -= (dx / distance) * force * 2;
          star.y -= (dy / distance) * force * 2;
        }

        // Gentle floating animation
        star.y -= star.speed;
        star.x += Math.sin(Date.now() * 0.001 + star.id) * 0.1;

        // Wrap around
        if (star.y < 0) star.y = canvas.height;
        if (star.x < 0) star.x = canvas.width;
        if (star.x > canvas.width) star.x = 0;

        // Draw star
        const gradient = ctx.createRadialGradient(
          star.x,
          star.y,
          0,
          star.x,
          star.y,
          star.size
        );

        // Alternate colors for cosmic effect
        if (star.id % 3 === 0) {
          gradient.addColorStop(0, `rgba(176, 67, 255, ${star.opacity})`);
          gradient.addColorStop(1, 'rgba(176, 67, 255, 0)');
        } else if (star.id % 3 === 1) {
          gradient.addColorStop(0, `rgba(43, 203, 255, ${star.opacity})`);
          gradient.addColorStop(1, 'rgba(43, 203, 255, 0)');
        } else {
          gradient.addColorStop(0, `rgba(61, 246, 255, ${star.opacity})`);
          gradient.addColorStop(1, 'rgba(61, 246, 255, 0)');
        }

        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2);
        ctx.fill();

        // Add glow for larger stars
        if (star.size > 1.5) {
          ctx.fillStyle = `rgba(255, 255, 255, ${star.opacity * 0.3})`;
          ctx.beginPath();
          ctx.arc(star.x, star.y, star.size * 0.5, 0, Math.PI * 2);
          ctx.fill();
        }
      });

      animationFrameId.current = requestAnimationFrame(animate);
    };
    animate();

    return () => {
      window.removeEventListener('resize', updateSize);
      window.removeEventListener('mousemove', handleMouseMove);
      if (animationFrameId.current !== null) {
        cancelAnimationFrame(animationFrameId.current);
      }
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none z-0"
      style={{ opacity: 0.6 }}
    />
  );
}
