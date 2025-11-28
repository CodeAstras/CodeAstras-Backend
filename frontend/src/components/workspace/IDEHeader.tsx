import { useNavigate } from "react-router-dom";

interface IDEHeaderProps {
  visible: boolean;
  onToggleParticipants?: () => void;
  onToggleVideo?: () => void;
  participantsOpen?: boolean;
  videoOpen?: boolean;
}

export function IDEHeader({
  visible,
  onToggleParticipants,
  onToggleVideo,
  participantsOpen,
  videoOpen,
}: IDEHeaderProps) {
  const navigate = useNavigate();

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-40 bg-[#0f0f0f]/95 backdrop-blur-md border-b border-white/5 transition-transform duration-300 ${
        visible ? "translate-y-0" : "-translate-y-full"
      }`}
    >
      <div className="flex items-center justify-between px-6 h-14">
        {/* Left nav links */}
        <nav className="flex items-center gap-8">
          <button
            type="button"
            onClick={() => navigate("/")}
            className="text-sm text-white/80 hover:text-[#0ea5e9] transition-colors"
          >
            ← Home
          </button>
          <button
            type="button"
            onClick={() => navigate("/")}
            className="text-sm text-white/60 hover:text-white/90 transition-colors"
          >
            Pricing
          </button>
          <button
            type="button"
            onClick={() => navigate("/")}
            className="text-sm text-white/60 hover:text-white/90 transition-colors"
          >
            About
          </button>
        </nav>

        {/* CodeAstra wordmark */}
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] animate-pulse" />
          <span className="bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] bg-clip-text text-transparent">
            CodeAstra
          </span>
        </div>

        {/* Right - Panel Controls */}
        <div className="flex items-center gap-2">
          {/* Chat & Participants Toggle */}
          <button
            onClick={onToggleParticipants}
            className={`group relative px-3 py-1.5 rounded-lg transition-all duration-300 flex items-center gap-2 ${
              participantsOpen
                ? "bg-gradient-to-r from-[#0ea5e9]/20 to-[#06b6d4]/20 border border-[#0ea5e9]/50"
                : "bg-white/5 border border-white/10 hover:border-[#0ea5e9]/30"
            }`}
            title={
              participantsOpen
                ? "Hide chat & participants"
                : "Show chat & participants"
            }
          >
            <svg
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z"
              />
            </svg>
            <span className="text-xs font-medium">Chat</span>
            {participantsOpen && (
              <div className="absolute -bottom-0.5 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-gradient-to-r from-[#0ea5e9] to-[#06b6d4] rounded-full" />
            )}
          </button>

          {/* Video Panel Toggle */}
          <button
            onClick={onToggleVideo}
            className={`group relative px-3 py-1.5 rounded-lg transition-all duration-300 flex items-center gap-2 ${
              videoOpen
                ? "bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border border-[#7c3aed]/50"
                : "bg-white/5 border border-white/10 hover:border-[#7c3aed]/30"
            }`}
            title={videoOpen ? "Hide video panel" : "Show video panel"}
          >
            <svg
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
              />
            </svg>
            <span className="text-xs font-medium">Video</span>
            {videoOpen && (
              <div className="absolute -bottom-0.5 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-full" />
            )}
          </button>
        </div>
      </div>
    </header>
  );
}
