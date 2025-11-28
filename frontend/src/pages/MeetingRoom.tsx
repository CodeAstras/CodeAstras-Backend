import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  MonitorUp,
  Video,
  VideoOff,
  Hand,
  UserPlus,
  Settings,
  LogOut,
  Users,
  Copy,
  Check,
  MessageCircle,
  Code2,
  PenTool,
  ScreenShare,
  ListTodo,
  Play,
  Send,
  Smile,
  Minimize2,
  Maximize2,
} from 'lucide-react';
import { CosmicStars } from "../components/workspace/CosmicStars";


type Tab = 'code' | 'whiteboard' | 'screen' | 'tasks';

export default function MeetingRoom() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<Tab>('code');
  const [isMuted, setIsMuted] = useState(false);
  const [isSpeakerOn, setIsSpeakerOn] = useState(true);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [isCameraOn, setIsCameraOn] = useState(false);
  const [isHandRaised, setIsHandRaised] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);
  const [chatMessage, setChatMessage] = useState('');
  const [isFullscreen, setIsFullscreen] = useState(false);

  const roomCode = 'ASTRO-4X9K';
  const roomName = 'Backend Team Room';

  // Mock participants with voice activity
  const [participants, setParticipants] = useState([
    { id: 1, name: 'Alex Chen', avatar: 'AC', color: '#7c3aed', speaking: false, muted: false, online: true },
    { id: 2, name: 'Sarah Kim', avatar: 'SK', color: '#0ea5e9', speaking: true, muted: false, online: true },
    { id: 3, name: 'Mike Ross', avatar: 'MR', color: '#06b6d4', speaking: false, muted: true, online: true },
    { id: 4, name: 'Emma Wilson', avatar: 'EW', color: '#8b5cf6', speaking: false, muted: false, online: true },
    { id: 5, name: 'You', avatar: 'YO', color: '#ec4899', speaking: false, muted: isMuted, online: true },
  ]);

  // Mock chat messages
  const chatMessages = [
    { id: 1, user: 'Sarah Kim', message: 'Hey team! Ready to review the API?', time: '2:30 PM', avatar: 'SK', color: '#0ea5e9' },
    { id: 2, user: 'Alex Chen', message: 'Yes! Let me share the endpoint code', time: '2:31 PM', avatar: 'AC', color: '#7c3aed' },
    { id: 3, user: 'Mike Ross', message: '```js\nconst api = "/v1/users"\n```', time: '2:32 PM', avatar: 'MR', color: '#06b6d4', isCode: true },
    { id: 4, user: 'Emma Wilson', message: 'Looks good! Should we test it?', time: '2:33 PM', avatar: 'EW', color: '#8b5cf6' },
  ];

  // Simulate voice activity animation
  useEffect(() => {
    const interval = setInterval(() => {
      setParticipants(prev =>
        prev.map(p => ({
          ...p,
          speaking: Math.random() > 0.7 && !p.muted && p.id !== 5,
        })),
      );
    }, 1500);
    return () => clearInterval(interval);
  }, []);

  const copyRoomCode = () => {
    navigator.clipboard.writeText(roomCode);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const tabs = [
    { id: 'code' as Tab, label: 'Live Code', icon: Code2 },
    { id: 'whiteboard' as Tab, label: 'Whiteboard', icon: PenTool },
    { id: 'screen' as Tab, label: 'Screen Share', icon: ScreenShare },
    { id: 'tasks' as Tab, label: 'Tasks', icon: ListTodo },
  ];

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-white overflow-hidden relative">
      {/* Cosmic background */}
      <CosmicStars />

      {/* Subtle background gradients */}
      <div className="fixed inset-0 pointer-events-none opacity-10">
        <div className="absolute top-0 right-1/3 w-[600px] h-[600px] bg-[#7c3aed] rounded-full blur-[150px]" />
        <div className="absolute bottom-0 left-1/3 w-[600px] h-[600px] bg-[#0ea5e9] rounded-full blur-[150px]" />
      </div>

      {/* Main Layout */}
      <div className="flex h-screen relative z-10">
        {/* LEFT SIDE - Main Workspace (70%) */}
        <div className={`transition-all duration-300 ${isFullscreen ? 'w-full' : 'w-[70%]'} flex flex-col`}>
          {/* Room Header */}
          <div className="bg-[#0f0f0f]/95 backdrop-blur-md border-b border-white/5 px-6 py-4">
            <div className="flex items-center justify-between">
              {/* Left - Room Info */}
              <div className="flex items-center gap-4">
                <div>
                  <h1 className="text-xl font-semibold mb-1">{roomName}</h1>
                  <div className="flex items-center gap-3">
                    <button
                      onClick={copyRoomCode}
                      className="flex items-center gap-2 px-3 py-1 bg-white/5 border border-white/10 rounded-lg hover:bg-white/10 transition-all group"
                    >
                      <span className="text-xs font-mono text-white/60">{roomCode}</span>
                      {copiedCode ? (
                        <Check className="w-3 h-3 text-green-400" />
                      ) : (
                        <Copy className="w-3 h-3 text-white/40 group-hover:text-white/60" />
                      )}
                    </button>
                    <div className="flex items-center gap-2 text-sm text-white/60">
                      <Users className="w-4 h-4" />
                      <span>{participants.length} participants</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right - Actions */}
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setIsFullscreen(!isFullscreen)}
                  className="p-2 hover:bg-white/5 rounded-lg transition-colors"
                  title={isFullscreen ? 'Exit fullscreen' : 'Fullscreen'}
                >
                  {isFullscreen ? (
                    <Minimize2 className="w-4 h-4 text-white/60" />
                  ) : (
                    <Maximize2 className="w-4 h-4 text-white/60" />
                  )}
                </button>
                <button
                  type="button"
                  onClick={() => navigate('/dashboard')}
                  className="flex items-center gap-2 px-4 py-2 bg-red-500/10 border border-red-500/30 rounded-lg hover:bg-red-500/20 transition-all text-red-400"
                >
                  <LogOut className="w-4 h-4" />
                  <span className="text-sm">Leave Room</span>
                </button>
              </div>
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="bg-[#0f0f0f]/50 border-b border-white/5 px-6 py-3">
            <div className="flex items-center gap-2">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`relative px-4 py-2 rounded-lg transition-all duration-300 flex items-center gap-2 ${
                      activeTab === tab.id
                        ? 'bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 text-white'
                        : 'text-white/60 hover:text-white/90 hover:bg-white/5'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span className="text-sm font-medium">{tab.label}</span>
                    {activeTab === tab.id && (
                      <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-full" />
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Main Content Area */}
          <div className="flex-1 overflow-hidden bg-[#0a0a0a]">
            {/* LIVE CODE TAB */}
            {activeTab === 'code' && (
              <div className="h-full flex flex-col">
                {/* Code Editor Toolbar */}
                <div className="bg-[#0f0f0f] border-b border-white/5 px-4 py-2 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <span className="text-sm text-white/60">main.py</span>
                    <div className="w-px h-4 bg-white/10" />
                    <select className="bg-white/5 border border-white/10 rounded px-3 py-1 text-sm text-white/80 focus:outline-none focus:border-[#7c3aed]/50">
                      <option>Python 3.11</option>
                      <option>JavaScript</option>
                      <option>TypeScript</option>
                    </select>
                  </div>
                  <button className="px-4 py-1.5 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm font-medium hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                    <Play className="w-3.5 h-3.5 fill-current" />
                    Run Code
                  </button>
                </div>

                {/* Monaco-style Editor */}
                <div className="flex-1 relative overflow-hidden">
                  <div className="absolute inset-0 font-mono text-sm p-6 overflow-auto">
                    {/* Line numbers and code */}
                    <div className="flex gap-6">
                      {/* Line numbers */}
                      <div className="text-white/30 select-none">
                        {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map(num => (
                          <div key={num} className="leading-6">{num}</div>
                        ))}
                      </div>

                      {/* Code content */}
                      <div className="flex-1">
                        <div className="leading-6">
                          <span className="text-purple-400">from</span>
                          <span className="text-white"> fastapi </span>
                          <span className="text-purple-400">import</span>
                          <span className="text-white"> FastAPI</span>
                        </div>
                        <div className="leading-6">
                          <span className="text-purple-400">from</span>
                          <span className="text-white"> pydantic </span>
                          <span className="text-purple-400">import</span>
                          <span className="text-white"> BaseModel</span>
                        </div>
                        <div className="leading-6">&nbsp;</div>
                        <div className="leading-6">
                          <span className="text-white">app </span>
                          <span className="text-purple-400">=</span>
                          <span className="text-white"> FastAPI()</span>
                        </div>
                        <div className="leading-6">&nbsp;</div>
                        <div className="leading-6 relative">
                          <span className="text-purple-400">class</span>
                          <span className="text-cyan-400"> User</span>
                          <span className="text-white">(BaseModel):</span>
                          {/* Collaborative cursor - Sarah */}
                          <div className="absolute right-0 top-0 flex items-center gap-1">
                            <div className="px-2 py-0.5 bg-[#0ea5e9] rounded text-[10px] font-sans">Sarah</div>
                            <div className="w-0.5 h-5 bg-[#0ea5e9] animate-pulse" />
                          </div>
                        </div>
                        <div className="leading-6 pl-8">
                          <span className="text-white">name: </span>
                          <span className="text-cyan-400">str</span>
                        </div>
                        <div className="leading-6 pl-8">
                          <span className="text-white">email: </span>
                          <span className="text-cyan-400">str</span>
                        </div>
                        <div className="leading-6">&nbsp;</div>
                        <div className="leading-6">
                          <span className="text-white/60"># API endpoint</span>
                        </div>
                        <div className="leading-6">
                          <span className="text-purple-400">@app</span>
                          <span className="text-white">.post(</span>
                          <span className="text-green-400">"/api/users"</span>
                          <span className="text-white">)</span>
                        </div>
                        <div className="leading-6">
                          <span className="text-purple-400">async def</span>
                          <span className="text-cyan-400"> create_user</span>
                          <span className="text-white">(user: User):</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Glow border effect */}
                  <div className="absolute inset-0 border border-[#7c3aed]/10 pointer-events-none rounded" />
                </div>

                {/* Output Panel */}
                <div className="h-32 bg-[#0f0f0f] border-t border-white/5 p-4 overflow-auto font-mono text-sm">
                  <div className="text-green-400">$ python main.py</div>
                  <div className="text-white/60 mt-2">INFO: Started server process</div>
                  <div className="text-white/60">INFO: Uvicorn running on http://127.0.0.1:8000</div>
                </div>
              </div>
            )}

            {/* WHITEBOARD TAB */}
            {activeTab === 'whiteboard' && (
              <div className="h-full flex items-center justify-center bg-[#0f0f0f]/30">
                <div className="text-center">
                  <PenTool className="w-16 h-16 text-white/20 mx-auto mb-4" />
                  <p className="text-white/40">Whiteboard feature coming soon</p>
                </div>
              </div>
            )}

            {/* SCREEN SHARE TAB */}
            {activeTab === 'screen' && (
              <div className="h-full flex items-center justify-center bg-[#0f0f0f]/30">
                <div className="text-center">
                  <ScreenShare className="w-16 h-16 text-white/20 mx-auto mb-4" />
                  <p className="text-white/40 mb-4">No active screen share</p>
                  <button className="px-6 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg hover:shadow-lg transition-all">
                    Share Your Screen
                  </button>
                </div>
              </div>
            )}

            {/* TASKS TAB */}
            {activeTab === 'tasks' && (
              <div className="h-full p-6 overflow-auto">
                <div className="max-w-2xl">
                  <h3 className="text-lg font-semibold mb-4">Team Tasks</h3>
                  <div className="space-y-2">
                    {['Implement user authentication', 'Fix database connection bug', 'Update API documentation'].map((task, idx) => (
                      <div key={idx} className="flex items-center gap-3 p-3 bg-white/5 border border-white/10 rounded-lg hover:bg-white/10 transition-all">
                        <input type="checkbox" className="w-4 h-4 rounded" />
                        <span className="text-sm">{task}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* RIGHT SIDE - Voice & Social Sidebar (30%) */}
        {!isFullscreen && (
          <div className="w-[30%] bg-[#0f0f0f] border-l border-white/5 flex flex-col">
            {/* Voice Chat Section */}
            <div className="border-b border-white/5 p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-white/80">Voice Channel</h3>
                <div className="flex items-center gap-1 px-2 py-1 bg-green-500/20 border border-green-500/30 rounded-full">
                  <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
                  <span className="text-xs text-green-400">Live</span>
                </div>
              </div>

              {/* Participant Cards */}
              <div className="space-y-2">
                {participants.map((participant) => (
                  <div
                    key={participant.id}
                    className={`relative p-3 rounded-xl transition-all duration-300 ${
                      participant.speaking
                        ? 'bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border border-[#7c3aed]/50'
                        : 'bg-white/5 border border-white/10'
                    }`}
                  >
                    {/* Speaking animation ring */}
                    {participant.speaking && (
                      <div
                        className="absolute inset-0 rounded-xl animate-pulse"
                        style={{
                          boxShadow: `0 0 20px ${participant.color}80`,
                        }}
                      />
                    )}

                    <div className="relative flex items-center gap-3">
                      {/* Avatar */}
                      <div className="relative">
                        <div
                          className="w-10 h-10 rounded-lg flex items-center justify-center font-semibold text-sm"
                          style={{
                            backgroundColor: `${participant.color}30`,
                            color: participant.color,
                          }}
                        >
                          {participant.avatar}
                        </div>
                        {participant.online && (
                          <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 border-2 border-[#0f0f0f] rounded-full" />
                        )}
                      </div>

                      {/* Name and status */}
                      <div className="flex-1">
                        <div className="text-sm font-medium">{participant.name}</div>
                        <div className="flex items-center gap-1.5 text-xs text-white/40">
                          {participant.speaking ? (
                            <>
                              <div className="flex items-center gap-0.5">
                                <div className="w-0.5 h-2 bg-[#0ea5e9] rounded animate-pulse" style={{ animationDelay: '0ms' }} />
                                <div className="w-0.5 h-3 bg-[#0ea5e9] rounded animate-pulse" style={{ animationDelay: '150ms' }} />
                                <div className="w-0.5 h-2 bg-[#0ea5e9] rounded animate-pulse" style={{ animationDelay: '300ms' }} />
                              </div>
                              <span className="text-[#0ea5e9]">Speaking...</span>
                            </>
                          ) : participant.muted ? (
                            <span className="text-red-400">Muted</span>
                          ) : (
                            <span>Connected</span>
                          )}
                        </div>
                      </div>

                      {/* Mute icon */}
                      {participant.muted && <MicOff className="w-3.5 h-3.5 text-red-400" />}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Chat Panel */}
            <div className="flex-1 flex flex-col overflow-hidden">
              {/* Chat Header */}
              <div className="p-4 border-b border-white/5">
                <h3 className="text-sm font-semibold text-white/80 flex items-center gap-2">
                  <MessageCircle className="w-4 h-4" />
                  Team Chat
                </h3>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {chatMessages.map((msg) => (
                  <div key={msg.id} className="group">
                    <div className="flex items-start gap-2">
                      <div
                        className="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-semibold flex-shrink-0"
                        style={{
                          backgroundColor: `${msg.color}30`,
                          color: msg.color,
                        }}
                      >
                        {msg.avatar}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-baseline gap-2 mb-1">
                          <span className="text-sm font-medium">{msg.user}</span>
                          <span className="text-xs text-white/30">{msg.time}</span>
                        </div>
                        {msg.isCode ? (
                          <div className="bg-[#1a1a1a] border border-white/10 rounded-lg p-3 font-mono text-xs text-[#0ea5e9]">
                            {msg.message}
                          </div>
                        ) : (
                          <p className="text-sm text-white/80">{msg.message}</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Chat Input */}
              <div className="p-4 border-t border-white/5">
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    value={chatMessage}
                    onChange={(e) => setChatMessage(e.target.value)}
                    placeholder="Type a message..."
                    className="flex-1 bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#7c3aed]/50 placeholder:text-white/30"
                  />
                  <button className="p-2 hover:bg-white/5 rounded-lg transition-colors">
                    <Smile className="w-5 h-5 text-white/60" />
                  </button>
                  <button className="p-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all">
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* FLOATING UTILITY DOCK */}
      <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50">
        <div className="bg-[#0f0f0f]/95 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl p-3">
          <div className="flex items-center gap-2">
            {/* Mic Toggle */}
            <button
              onClick={() => setIsMuted(!isMuted)}
              className={`p-3 rounded-xl transition-all duration-300 ${
                isMuted
                  ? 'bg-red-500/20 text-red-400 border border-red-500/50'
                  : 'bg-white/5 hover:bg-white/10 border border-white/10'
              }`}
              title={isMuted ? 'Unmute' : 'Mute'}
            >
              {isMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
            </button>

            {/* Speaker Toggle */}
            <button
              onClick={() => setIsSpeakerOn(!isSpeakerOn)}
              className={`p-3 rounded-xl transition-all duration-300 ${
                !isSpeakerOn
                  ? 'bg-red-500/20 text-red-400 border border-red-500/50'
                  : 'bg-white/5 hover:bg-white/10 border border-white/10'
              }`}
              title={isSpeakerOn ? 'Mute speaker' : 'Unmute speaker'}
            >
              {isSpeakerOn ? <Volume2 className="w-5 h-5" /> : <VolumeX className="w-5 h-5" />}
            </button>

            <div className="w-px h-8 bg-white/10" />

            {/* Screen Share */}
            <button
              onClick={() => setIsScreenSharing(!isScreenSharing)}
              className={`p-3 rounded-xl transition-all duration-300 ${
                isScreenSharing
                  ? 'bg-gradient-to-r from-[#7c3aed]/30 to-[#0ea5e9]/30 border border-[#7c3aed]/50'
                  : 'bg-white/5 hover:bg-white/10 border border-white/10'
              }`}
              title="Screen share"
            >
              <MonitorUp className="w-5 h-5" />
            </button>

            {/* Camera Toggle */}
            <button
              onClick={() => setIsCameraOn(!isCameraOn)}
              className={`p-3 rounded-xl transition-all duration-300 ${
                isCameraOn
                  ? 'bg-gradient-to-r from-[#7c3aed]/30 to-[#0ea5e9]/30 border border-[#7c3aed]/50'
                  : 'bg-white/5 hover:bg-white/10 border border-white/10'
              }`}
              title={isCameraOn ? 'Turn off camera' : 'Turn on camera'}
            >
              {isCameraOn ? <Video className="w-5 h-5" /> : <VideoOff className="w-5 h-5" />}
            </button>

            <div className="w-px h-8 bg-white/10" />

            {/* Raise Hand */}
            <button
              onClick={() => setIsHandRaised(!isHandRaised)}
              className={`p-3 rounded-xl transition-all duration-300 ${
                isHandRaised
                  ? 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/50'
                  : 'bg-white/5 hover:bg-white/10 border border-white/10'
              }`}
              title="Raise hand"
            >
              <Hand className="w-5 h-5" />
            </button>

            {/* Invite */}
            <button
              className="p-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 transition-all"
              title="Invite members"
            >
              <UserPlus className="w-5 h-5" />
            </button>

            {/* Settings */}
            <button
              className="p-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 transition-all"
              title="Settings"
            >
              <Settings className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
