import { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { 
  Code2, 
  Users, 
  Folder, 
  Bell, 
  Plus, 
  Video, 
  Play, 
  GitBranch, 
  Clock, 
  Zap,
  Cloud,
  History,
  BarChart3,
  UserPlus,
  MessageCircle,
  Settings
} from 'lucide-react';
import { CosmicStars } from "../components/workspace/CosmicStars";


export default function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('home');

  // Mock data
  const projects = [
    { id: 1, name: 'E-Commerce API', language: 'TypeScript', lastEdited: '2 hours ago', members: ['A', 'B', 'C'], color: '#7c3aed' },
    { id: 2, name: 'Chat Application', language: 'React', lastEdited: '5 hours ago', members: ['D', 'E'], color: '#0ea5e9' },
    { id: 3, name: 'AI Dashboard', language: 'Python', lastEdited: '1 day ago', members: ['F', 'G', 'H'], color: '#06b6d4' },
    { id: 4, name: 'Mobile App Backend', language: 'Node.js', lastEdited: '2 days ago', members: ['I'], color: '#8b5cf6' },
  ];

  const rooms = [
    { id: 1, name: 'Frontend Sprint', participants: 5, active: true, color: '#7c3aed' },
    { id: 2, name: 'API Development', participants: 3, active: true, color: '#0ea5e9' },
    { id: 3, name: 'Bug Bash Session', participants: 8, active: true, color: '#06b6d4' },
  ];

  const teams = [
    { id: 1, name: 'Core Team', members: ['A', 'B', 'C', 'D', 'E'], color: '#7c3aed' },
    { id: 2, name: 'Design Squad', members: ['F', 'G', 'H'], color: '#0ea5e9' },
  ];

  const friends = [
    { id: 1, name: 'Alex Chen', status: 'online', avatar: 'AC', color: '#7c3aed' },
    { id: 2, name: 'Sarah Kim', status: 'online', avatar: 'SK', color: '#0ea5e9' },
    { id: 3, name: 'Mike Ross', status: 'offline', avatar: 'MR', color: '#06b6d4' },
    { id: 4, name: 'Emma Wilson', status: 'online', avatar: 'EW', color: '#8b5cf6' },
  ];

  const features = [
    { icon: Zap, title: 'Real-Time Code Sync', desc: 'Collaborate live with zero lag', color: '#7c3aed' },
    { icon: Video, title: 'Built-in Voice Chat', desc: 'Communicate without switching apps', color: '#0ea5e9' },
    { icon: Play, title: 'Live Code Execution', desc: 'Docker sandbox with instant results', color: '#06b6d4' },
    { icon: Folder, title: 'Multi-File Projects', desc: 'Full project support with structure', color: '#8b5cf6' },
    { icon: History, title: 'Version History', desc: 'Track every change with timeline', color: '#ec4899' },
    { icon: BarChart3, title: 'Collaboration Analytics', desc: 'Insights on team productivity', color: '#10b981' },
  ];

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      {/* Cosmic background */}
      <CosmicStars />
      
      {/* Subtle background gradients */}
      <div className="fixed inset-0 pointer-events-none opacity-10">
        <div className="absolute top-0 right-1/4 w-[600px] h-[600px] bg-[#7c3aed] rounded-full blur-[150px]" />
        <div className="absolute bottom-0 left-1/4 w-[600px] h-[600px] bg-[#0ea5e9] rounded-full blur-[150px]" />
      </div>

      {/* Header Navigation */}
      <header className="fixed top-0 left-0 right-0 z-50 bg-[#0f0f0f]/95 backdrop-blur-md border-b border-white/5">
        <div className="max-w-[1800px] mx-auto px-6 h-16 flex items-center justify-between">
          {/* Logo */}
          <button
            type="button"
            onClick={() => navigate('/')}
            className="flex items-center gap-3 group"
          >
            <div className="relative">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center">
                <Code2 className="w-5 h-5" />
              </div>
              <div className="absolute inset-0 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] blur-md opacity-50 group-hover:opacity-75 transition-opacity" />
            </div>
            <span className="text-xl font-semibold bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] bg-clip-text text-transparent">
              CodeAstra
            </span>
          </button>

          {/* Navigation Menu */}
          <nav className="flex items-center gap-1">
            {['Home', 'My Projects', 'Rooms', 'Teams', 'Friends'].map((item) => (
              <button
                key={item}
                onClick={() => setActiveTab(item.toLowerCase().replace(' ', '-'))}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  activeTab === item.toLowerCase().replace(' ', '-')
                    ? 'bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 text-white border border-[#7c3aed]/30'
                    : 'text-white/60 hover:text-white/90 hover:bg-white/5'
                }`}
              >
                {item}
              </button>
            ))}
          </nav>

          {/* Right Icons */}
          <div className="flex items-center gap-3">
            <button className="relative p-2 rounded-lg hover:bg:white/5 transition-colors">
              <Bell className="w-5 h-5 text-white/60" />
              <div className="absolute top-1 right-1 w-2 h-2 bg-[#0ea5e9] rounded-full animate-pulse" />
            </button>
            <button
              type="button"
              onClick={() => navigate('/profile')}
              className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center font-semibold cursor-pointer hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all"
            >
              AC
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="pt-24 pb-16 px-6 max-w-[1800px] mx-auto relative z-10">
        {/* Hero Panel */}
        <section className="mb-12">
          <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#1a1a2e] to-[#0a0a0a] border border-white/10 p-12">
            {/* Holographic background effect */}
            <div className="absolute inset-0 opacity-20">
              <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-[#7c3aed]/30 via-transparent to-[#0ea5e9]/30" />
              <div className="absolute top-1/4 left-1/4 w-64 h-64 bg-[#7c3aed] rounded-full blur-[100px]" />
              <div className="absolute bottom-1/4 right-1/4 w-64 h-64 bg-[#0ea5e9] rounded-full blur-[100px]" />
            </div>

            <div className="relative z-10 flex items-center justify-between">
              <div className="flex-1">
                <h1 className="text-5xl font-bold mb-3 bg-gradient-to-r from:white via:white to-white/70 bg-clip-text text-transparent">
                  Welcome to CodeAstra
                </h1>
                <p className="text-xl text-white/60 mb-8">
                  Your collaborative coding workspace in the cloud
                </p>
                
                <div className="flex items-center gap-4">
                  <button className="px-6 py-3 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-xl font-medium hover:shadow-xl hover:shadow-[#7c3aed]/40 transition-all duration-300 flex items-center gap-2">
                    <Plus className="w-5 h-5" />
                    Create Room
                  </button>
                  <button 
                    onClick={() => navigate('/team')}
                    className="px-6 py-3 bg-white/5 border border-white/10 rounded-xl font-medium hover:bg-white/10 transition-all duration-300 flex items-center gap-2"
                  >
                    <Users className="w-5 h-5" />
                    Team Workspace
                  </button>
                  <button className="px-6 py-3 bg:white/5 border border-white/10 rounded-xl font-medium hover:bg:white/10 transition-all duration-300 flex items-center gap-2">
                    <Folder className="w-5 h-5" />
                    Start New Project
                  </button>
                  <button className="px-6 py-3 bg:white/5 border border-white/10 rounded-xl font-medium hover:bg:white/10 transition-all duration-300 flex items-center gap-2">
                    <Code2 className="w-5 h-5" />
                    Join via Code
                  </button>
                </div>
              </div>

              {/* Decorative code visualization */}
              <div className="hidden lg:block">
                <div className="w-64 h-64 relative">
                  <div className="absolute inset-0 bg-gradient-to-br from-[#7c3aed]/20 to-[#0ea5e9]/20 rounded-2xl backdrop-blur-sm border border-white/10 p-6">
                    <div className="space-y-3 font-mono text-xs">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full bg-[#7c3aed]" />
                        <span className="text-white/40">const</span>
                        <span className="text-[#0ea5e9]">project</span>
                      </div>
                      <div className="flex items-center gap-2 ml-4">
                        <span className="text-white/40">team:</span>
                        <span className="text-[#06b6d4]">"collaborative"</span>
                      </div>
                      <div className="flex items-center gap-2 ml-4">
                        <span className="text-white/40">status:</span>
                        <span className="text-green-400">"active"</span>
                      </div>
                      <div className="w-full h-px bg-white/10 my-3" />
                      <div className="flex items-center gap-2">
                        <Play className="w-3 h-3 text-[#0ea5e9]" />
                        <span className="text-white/40">Running...</span>
                      </div>
                    </div>
                  </div>
                  <div className="absolute inset-0 bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] rounded-2xl blur-2xl opacity-20" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Main Grid Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-12">
          {/* Your Projects Section */}
          <section className="lg:col-span-2">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-semibold flex items-center gap-3">
                <Folder className="w-6 h-6 text-[#7c3aed]" />
                Your Projects
              </h2>
              <button className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all flex items-center gap-2">
                <Plus className="w-4 h-4" />
                New Project
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {projects.map((project) => (
                <div
                  key={project.id}
                  className="group relative bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 hover:border-[#7c3aed]/30 transition-all duration-300 hover:shadow-xl hover:shadow-[#7c3aed]/10"
                >
                  {/* Glow effect on hover */}
                  <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-[#7c3aed]/0 to-[#0ea5e9]/0 group-hover:from-[#7c3aed]/5 group-hover:to-[#0ea5e9]/5 transition-all duration-300" />
                  
                  <div className="relative z-10">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h3 className="text-lg font-semibold mb-1">{project.name}</h3>
                        <span className="text-xs text-white/40 font-mono">{project.language}</span>
                      </div>
                      <div
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: project.color, boxShadow: `0 0 10px ${project.color}80` }}
                      />
                    </div>

                    <div className="flex items-center gap-2 text-xs text-white/50 mb-4">
                      <Clock className="w-3 h-3" />
                      {project.lastEdited}
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="flex -space-x-2">
                        {project.members.map((member, idx) => (
                          <div
                            key={idx}
                            className="w-7 h-7 rounded-full bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center text-xs font-semibold border-2 border-[#0f0f0f]"
                          >
                            {member}
                          </div>
                        ))}
                      </div>
                      <button 
                        onClick={() => navigate('/workspace')}
                        className="px-4 py-1.5 bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border border-[#7c3aed]/30 rounded-lg text-sm hover:from-[#7c3aed]/30 hover:to-[#0ea5e9]/30 transition-all"
                      >
                        Open
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Friends Section */}
          <section>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-semibold flex items-center gap-3">
                <Users className="w-6 h-6 text-[#0ea5e9]" />
                Friends
              </h2>
              <button className="p-2 hover:bg-white/5 rounded-lg transition-colors">
                <UserPlus className="w-5 h-5 text-white/60" />
              </button>
            </div>

            <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-4 space-y-2">
              {friends.map((friend) => (
                <div
                  key={friend.id}
                  className="flex items-center justify-between p-3 rounded-xl hover:bg-white/5 transition-all group"
                >
                  <div className="flex items-center gap-3">
                    <div className="relative">
                      <div
                        className="w-10 h-10 rounded-lg flex items-center justify-center font-semibold text-sm"
                        style={{ backgroundColor: `${friend.color}20`, color: friend.color }}
                      >
                        {friend.avatar}
                      </div>
                      <div
                        className={`absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full border-2 border-[#0f0f0f] ${
                          friend.status === 'online' ? 'bg-green-500' : 'bg-white/20'
                        }`}
                      />
                    </div>
                    <div>
                      <div className="text-sm font-medium">{friend.name}</div>
                      <div className="text-xs text-white/40 capitalize">{friend.status}</div>
                    </div>
                  </div>
                  <button className="opacity-0 group-hover:opacity-100 p-2 hover:bg-white/10 rounded-lg transition-all">
                    <MessageCircle className="w-4 h-4 text-white/60" />
                  </button>
                </div>
              ))}
            </div>
          </section>
        </div>

        {/* Your Rooms Section */}
        <section className="mb-12">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-semibold flex items-center gap-3">
              <Users className="w-6 h-6 text-[#0ea5e9]" />
              Your Rooms
            </h2>
            <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Create Room
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {rooms.map((room) => (
              <div
                key={room.id}
                className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 hover:border-[#0ea5e9]/30 transition-all duration-300 group hover:shadow-xl hover:shadow-[#0ea5e9]/10"
              >
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-semibold mb-2">{room.name}</h3>
                    <div className="flex items-center gap-2 text-sm text-white/50">
                      <Users className="w-4 h-4" />
                      {room.participants} participants
                    </div>
                  </div>
                  {room.active && (
                    <div className="flex items-center gap-1.5 px-2 py-1 bg-green-500/20 border border-green-500/30 rounded-full">
                      <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
                      <span className="text-xs text-green-400">Live</span>
                    </div>
                  )}
                </div>

                <button 
                  onClick={() => navigate('/room')}
                  className="w-full px-4 py-2 bg-gradient-to-r from-[#0ea5e9]/20 to-[#06b6d4]/20 border border-[#0ea5e9]/30 rounded-xl text-sm font-medium hover:from-[#0ea5e9]/30 hover:to-[#06b6d4]/30 transition-all"
                >
                  Join Room
                </button>
              </div>
            ))}
          </div>
        </section>

        {/* Teams Section */}
        <section className="mb-12">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-semibold flex items-center gap-3">
              <Users className="w-6 h-6 text-[#06b6d4]" />
              Teams
            </h2>
            <button className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Create Team
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {teams.map((team) => (
              <div
                key={team.id}
                className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 hover:border-[#06b6d4]/30 transition-all duration-300"
              >
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-semibold mb-2">{team.name}</h3>
                    <div className="text-sm text-white/50">{team.members.length} members</div>
                  </div>
                </div>

                <div className="flex -space-x-2 mb-4">
                  {team.members.map((member, idx) => (
                    <div
                      key={idx}
                      className="w-9 h-9 rounded-full bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center text-sm font-semibold border-2 border-[#0f0f0f]"
                    >
                      {member}
                    </div>
                  ))}
                </div>

                <div className="flex gap-2">
                  <button className="flex-1 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all">
                    View Workspace
                  </button>
                  <button className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all">
                    <UserPlus className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Feature Highlights */}
        <section>
          <h2 className="text-2xl font-semibold mb-6 flex items-center gap-3">
            <Zap className="w-6 h-6 text-[#7c3aed]" />
            Platform Features
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {features.map((feature, idx) => {
              const Icon = feature.icon;
              return (
                <div
                  key={idx}
                  className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 hover:border-white/10 transition-all duration-300 group"
                >
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center mb-4 transition-all duration-300 group-hover:scale-110"
                    style={{
                      backgroundColor: `${feature.color}15`,
                      boxShadow: `0 0 20px ${feature.color}20`,
                    }}
                  >
                    <Icon className="w-6 h-6" style={{ color: feature.color }} />
                  </div>
                  <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                  <p className="text-sm text-white/50">{feature.desc}</p>
                </div>
              );
            })}
          </div>
        </section>
      </main>
    </div>
  );
}
