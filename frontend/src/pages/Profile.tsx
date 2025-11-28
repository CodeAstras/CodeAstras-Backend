import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  User,
  Award,
  Zap,
  Settings,
  TrendingUp,
  LogOut,
  MapPin,
  Mail,
  Calendar,
  Code2,
  Users,
  Flame,
  GitBranch,
  Lock,
  Shield,
  Mic,
  Volume2,
  Palette,
  Chrome,
  Star,
  Target,
  BookOpen,
  Video,
  Trophy,
  Moon,
  Github,
  ChevronRight,
  Edit3, 
  Camera,
} from 'lucide-react';
import { CosmicStars } from "../components/workspace/CosmicStars";


type TabView = 'profile' | 'badges' | 'streaks' | 'contributions' | 'settings' | 'preferences';

export default function Profile() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<TabView>('profile');
  const [editingBio, setEditingBio] = useState(false);

  // User data
  const userData = {
    name: 'Alex Chen',
    username: '@dev_astro',
    bio: 'Full-stack developer. Building cool things in the cosmos. 🚀',
    location: 'San Francisco, CA',
    email: 'alex.chen@codeastra.dev',
    joinDate: 'January 2024',
    avatar: 'AC',
    color: '#7c3aed',
  };

  // Stats
  const stats = [
    { label: 'Projects', value: 24, icon: Code2, color: '#7c3aed' },
    { label: 'Rooms Joined', value: 156, icon: Users, color: '#0ea5e9' },
    { label: 'Contributions', value: 1247, icon: GitBranch, color: '#06b6d4' },
    { label: 'Streak', value: '42 days', icon: Flame, color: '#f59e0b' },
    { label: 'Languages', value: 8, icon: Code2, color: '#8b5cf6' },
  ];

  // Badges
  const badges = [
    { id: 1, name: 'Early User', description: 'Joined in the first month', icon: Star, tier: 'gold', unlocked: true },
    { id: 2, name: 'Daily Streak x7', description: '7 days coding streak', icon: Flame, tier: 'silver', unlocked: true },
    { id: 3, name: 'Team Player', description: 'Collaborated 50+ times', icon: Users, tier: 'gold', unlocked: true },
    { id: 4, name: 'Room Leader', description: 'Hosted 10+ rooms', icon: Video, tier: 'silver', unlocked: true },
    { id: 5, name: '1000 Lines', description: 'Wrote 1000+ lines of code', icon: Code2, tier: 'bronze', unlocked: true },
    { id: 6, name: 'Astra Master', description: 'Complete all achievements', icon: Trophy, tier: 'diamond', unlocked: false },
    { id: 7, name: 'Night Owl', description: 'Code after midnight 10 times', icon: Moon, tier: 'bronze', unlocked: true },
    { id: 8, name: 'Code Mentor', description: 'Help 20+ developers', icon: BookOpen, tier: 'silver', unlocked: false },
  ];

  // Contribution data (simulated)
  const generateContributions = () => {
    const contributions: { date: string; count: number }[] = [];
    const today = new Date();

    for (let i = 364; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const count = Math.floor(Math.random() * 15);
      contributions.push({
        date: date.toISOString().split('T')[0],
        count,
      });
    }
    return contributions;
  };

  const contributions = generateContributions();

  // Activity timeline
  const activities = [
    { id: 1, type: 'room', text: 'Joined room "Backend Sprint Planning"', time: '2 hours ago', icon: Video, color: '#7c3aed' },
    { id: 2, type: 'project', text: 'Created new project "E-Commerce API"', time: '5 hours ago', icon: Code2, color: '#0ea5e9' },
    { id: 3, type: 'badge', text: 'Earned badge "Team Player"', time: '1 day ago', icon: Award, color: '#f59e0b' },
    { id: 4, type: 'workspace', text: 'Updated Frontend Squad workspace', time: '1 day ago', icon: Users, color: '#06b6d4' },
    { id: 5, type: 'streak', text: 'Achieved 42-day coding streak 🔥', time: '2 days ago', icon: Flame, color: '#f59e0b' },
    { id: 6, type: 'collab', text: 'Collaborated with Sarah Kim on Analytics Dashboard', time: '3 days ago', icon: GitBranch, color: '#8b5cf6' },
  ];

  const getTierColor = (tier: string) => {
    switch (tier) {
      case 'diamond':
        return '#06b6d4';
      case 'gold':
        return '#f59e0b';
      case 'silver':
        return '#94a3b8';
      case 'bronze':
        return '#ca8a04';
      default:
        return '#7c3aed';
    }
  };

  const getContributionColor = (count: number) => {
    if (count === 0) return '#1a1a1a';
    if (count <= 3) return '#7c3aed20';
    if (count <= 6) return '#7c3aed40';
    if (count <= 9) return '#7c3aed60';
    if (count <= 12) return '#7c3aed80';
    return '#7c3aed';
  };

  const sidebarItems = [
    { id: 'profile' as TabView, label: 'Profile', icon: User },
    { id: 'badges' as TabView, label: 'Badges', icon: Award },
    { id: 'streaks' as TabView, label: 'Streaks', icon: Flame },
    { id: 'contributions' as TabView, label: 'Contributions', icon: TrendingUp },
    { id: 'settings' as TabView, label: 'Settings', icon: Settings },
    { id: 'preferences' as TabView, label: 'Preferences', icon: Palette },
  ];

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-white relative overflow-hidden">
      {/* Cosmic background */}
      <CosmicStars />

      {/* Subtle background gradients */}
      <div className="fixed inset-0 pointer-events-none opacity-10">
        <div className="absolute top-0 right-1/4 w-[600px] h-[600px] bg-[#7c3aed] rounded-full blur-[150px]" />
        <div className="absolute bottom-0 left-1/4 w-[600px] h-[600px] bg-[#0ea5e9] rounded-full blur-[150px]" />
      </div>

      {/* Header */}
      <header className="fixed top-0 left-0 right-0 z-50 bg-[#0f0f0f]/95 backdrop-blur-md border-b border-white/5">
        <div className="px-6 h-16 flex items-center justify-between">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="flex items-center gap-3 hover:opacity-80 transition-opacity"
          >
            <div className="relative">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center">
                <Code2 className="w-5 h-5" />
              </div>
              <div className="absolute inset-0 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] blur-md opacity-50" />
            </div>
            <span className="text-sm text-white/60">CodeAstra</span>
          </button>

          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="px-4 py-2 text-sm text-white/60 hover:text-white transition-colors"
            >
              Dashboard
            </button>
            <button
              type="button"
              onClick={() => navigate('/team')}
              className="px-4 py-2 text-sm text-white/60 hover:text-white transition-colors"
            >
              Team
            </button>
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="flex items-center gap-2 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all"
            >
              <LogOut className="w-4 h-4" />
              Sign Out
            </button>
          </div>
        </div>
      </header>

      {/* Main Layout */}
      <div className="flex pt-16 min-h-screen">
        {/* Sidebar Navigation */}
        <aside className="w-64 bg-[#0f0f0f] border-r border-white/5 p-4 fixed left-0 top-16 bottom-0 overflow-y-auto z-40">
          <div className="space-y-2">
            {sidebarItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 ${
                    activeTab === item.id
                      ? 'bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border border-[#7c3aed]/50 text-white'
                      : 'text-white/60 hover:bg-white/5 hover:text-white'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span className="font-medium">{item.label}</span>
                  {activeTab === item.id && <ChevronRight className="w-4 h-4 ml-auto" />}
                </button>
              );
            })}

            <div className="pt-4 mt-4 border-t border-white/5">
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-red-400 hover:bg-red-500/10 transition-all"
              >
                <LogOut className="w-5 h-5" />
                <span className="font-medium">Log Out</span>
              </button>
            </div>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 ml-64 p-8 relative z-10">
          {/* PROFILE TAB */}
          {activeTab === 'profile' && (
            <div className="space-y-8">
              {/* Profile Header */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl overflow-hidden">
                {/* Banner */}
                <div className="h-40 bg-[#0a0a0a] relative overflow-hidden">
                  {/* Cosmic gradient overlay */}
                  <div className="absolute inset-0 bg-gradient-to-r from-[#7c3aed]/30 via-[#0ea5e9]/20 to-[#7c3aed]/30" />

                  {/* Grid pattern */}
                  <div className="absolute inset-0 opacity-20">
                    <div
                      className="absolute inset-0"
                      style={{
                        backgroundImage:
                          'radial-gradient(circle at 2px 2px, rgba(124,58,237,0.4) 1px, transparent 0)',
                        backgroundSize: '32px 32px',
                      }}
                    />
                  </div>

                  {/* Glowing orbs */}
                  <div className="absolute top-0 left-1/4 w-32 h-32 bg-[#7c3aed] rounded-full blur-3xl opacity-30" />
                  <div className="absolute bottom-0 right-1/3 w-40 h-40 bg-[#0ea5e9] rounded-full blur-3xl opacity-20" />

                  {/* Stars */}
                  <div className="absolute inset-0">
                    {Array.from({ length: 30 }).map((_, i) => (
                      <div
                        key={i}
                        className="absolute w-1 h-1 bg-white rounded-full animate-pulse"
                        style={{
                          left: `${Math.random() * 100}%`,
                          top: `${Math.random() * 100}%`,
                          opacity: Math.random() * 0.7 + 0.3,
                          animationDelay: `${Math.random() * 3}s`,
                          animationDuration: `${Math.random() * 2 + 2}s`,
                        }}
                      />
                    ))}
                  </div>
                </div>

                {/* Profile Info */}
                <div className="px-8 pb-8">
                  <div className="flex items-end gap-6 -mt-16 mb-6">
                    {/* Avatar */}
                    <div className="relative group">
                      <div
                        className="w-32 h-32 rounded-2xl flex items-center justify-center text-3xl font-bold border-4 border-[#0f0f0f] relative z-10"
                        style={{ backgroundColor: `${userData.color}30`, color: userData.color }}
                      >
                        {userData.avatar}
                      </div>
                      <div
                        className="absolute inset-0 rounded-2xl blur-xl opacity-70"
                        style={{ backgroundColor: userData.color }}
                      />
                      <button className="absolute bottom-2 right-2 p-2 bg-[#0f0f0f] border border-white/10 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity z-20">
                        <Camera className="w-4 h-4" />
                      </button>
                    </div>

                    <div className="flex-1 pt-8">
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h1 className="text-3xl font-bold mb-1">{userData.name}</h1>
                          <p className="text-lg text-white/60">{userData.username}</p>
                        </div>
                        <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-xl hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                          <Edit3 className="w-4 h-4" />
                          Edit Profile
                        </button>
                      </div>

                      <p className="text-white/70 mb-4 max-w-2xl">{userData.bio}</p>

                      <div className="flex items-center gap-6 text-sm text-white/60">
                        <div className="flex items-center gap-2">
                          <MapPin className="w-4 h-4" />
                          {userData.location}
                        </div>
                        <div className="flex items-center gap-2">
                          <Mail className="w-4 h-4" />
                          {userData.email}
                        </div>
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4" />
                          Joined {userData.joinDate}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Quick Stats */}
                  <div className="grid grid-cols-5 gap-4">
                    {stats.map((stat) => {
                      const Icon = stat.icon;
                      return (
                        <div
                          key={stat.label}
                          className="bg-white/5 border border-white/10 rounded-2xl p-4 hover:border-white/20 transition-all group"
                        >
                          <div className="flex items-center gap-3 mb-2">
                            <div className="p-2 rounded-lg" style={{ backgroundColor: `${stat.color}20` }}>
                              <Icon className="w-5 h-5" style={{ color: stat.color }} />
                            </div>
                          </div>
                          <div className="text-2xl font-bold mb-1">{stat.value}</div>
                          <div className="text-sm text-white/60">{stat.label}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* Activity Timeline */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-2xl font-bold mb-6 flex items-center gap-3">
                  <TrendingUp className="w-6 h-6 text-[#7c3aed]" />
                  Recent Activity
                </h2>

                <div className="space-y-4">
                  {activities.map((activity, idx) => {
                    const Icon = activity.icon;
                    return (
                      <div key={activity.id} className="relative pl-12">
                        {/* Timeline line */}
                        {idx < activities.length - 1 && (
                          <div className="absolute left-5 top-10 w-px h-full bg-gradient-to-b from-white/20 to-transparent" />
                        )}

                        {/* Icon */}
                        <div
                          className="absolute left-0 top-1 w-10 h-10 rounded-xl flex items-center justify-center"
                          style={{
                            backgroundColor: `${activity.color}20`,
                            boxShadow: `0 0 20px ${activity.color}40`,
                          }}
                        >
                          <Icon className="w-5 h-5" style={{ color: activity.color }} />
                        </div>

                        <div className="bg-white/5 border border-white/10 rounded-xl p-4 hover:bg-white/10 transition-all">
                          <p className="text-white/90 mb-1">{activity.text}</p>
                          <p className="text-xs text-white/40">{activity.time}</p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          )}

          {/* BADGES TAB */}
          {activeTab === 'badges' && (
            <div className="space-y-8">
              <div>
                <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
                  <Award className="w-8 h-8 text-[#7c3aed]" />
                  Achievement Badges
                </h1>
                <p className="text-white/60">
                  {badges.filter((b) => b.unlocked).length} of {badges.length} badges earned
                </p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {badges.map((badge) => {
                  const Icon = badge.icon;
                  const tierColor = getTierColor(badge.tier);
                  return (
                    <div
                      key={badge.id}
                      className={`relative bg-[#0f0f0f] border rounded-2xl p-6 transition-all duration-300 group ${
                        badge.unlocked
                          ? 'border-white/10 hover:border-white/30 cursor-pointer'
                          : 'border-white/5 opacity-50'
                      }`}
                    >
                      {/* Lock overlay for locked badges */}
                      {!badge.unlocked && (
                        <div className="absolute inset-0 flex items-center justify-center bg-black/50 rounded-2xl backdrop-blur-sm">
                          <Lock className="w-8 h-8 text-white/30" />
                        </div>
                      )}

                      {/* Glow effect */}
                      {badge.unlocked && (
                        <div
                          className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity blur-xl"
                          style={{ backgroundColor: `${tierColor}20` }}
                        />
                      )}

                      <div className="relative">
                        <div
                          className="w-16 h-16 rounded-2xl flex items-center justify-center mb-4 mx-auto"
                          style={{
                            backgroundColor: badge.unlocked ? `${tierColor}20` : '#1a1a1a',
                            border: `2px solid ${badge.unlocked ? tierColor : '#2a2a2a'}`,
                            boxShadow: badge.unlocked ? `0 0 20px ${tierColor}40` : 'none',
                          }}
                        >
                          <Icon className="w-8 h-8" style={{ color: badge.unlocked ? tierColor : '#4a4a4a' }} />
                        </div>

                        <div className="text-center">
                          <div className="flex items-center justify-center gap-2 mb-2">
                            <h3 className="font-semibold">{badge.name}</h3>
                            <div
                              className="px-2 py-0.5 rounded-full text-xs font-medium capitalize"
                              style={{
                                backgroundColor: `${tierColor}20`,
                                color: tierColor,
                                border: `1px solid ${tierColor}40`,
                              }}
                            >
                              {badge.tier}
                            </div>
                          </div>
                          <p className="text-sm text-white/60">{badge.description}</p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* STREAKS TAB */}
          {activeTab === 'streaks' && (
            <div className="space-y-8">
              <div>
                <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
                  <Flame className="w-8 h-8 text-[#f59e0b]" />
                  Coding Streaks
                </h1>
                <p className="text-white/60">Keep the fire burning! 🔥</p>
              </div>

              {/* Current Streak */}
              <div className="bg-gradient-to-r from-[#f59e0b]/20 to-[#ec4899]/20 border border-[#f59e0b]/30 rounded-3xl p-8">
                <div className="text-center">
                  <Flame className="w-16 h-16 text-[#f59e0b] mx-auto mb-4" />
                  <div className="text-6xl font-bold mb-2 bg-gradient-to-r from-[#f59e0b] to-[#ec4899] bg-clip-text text-transparent">
                    42 Days
                  </div>
                  <p className="text-xl text-white/80 mb-4">Current Streak</p>
                  <p className="text-white/60">Don't break the chain! Code today to continue.</p>
                </div>
              </div>

              {/* Streak Stats */}
              <div className="grid grid-cols-3 gap-4">
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 text-center">
                  <div className="text-3xl font-bold text-[#7c3aed] mb-2">42</div>
                  <div className="text-sm text-white/60">Current Streak</div>
                </div>
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 text-center">
                  <div className="text-3xl font-bold text-[#0ea5e9] mb-2">89</div>
                  <div className="text-sm text-white/60">Longest Streak</div>
                </div>
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 text-center">
                  <div className="text-3xl font-bold text-[#f59e0b] mb-2">247</div>
                  <div className="text-sm text-white/60">Total Days</div>
                </div>
              </div>
            </div>
          )}

          {/* CONTRIBUTIONS TAB */}
          {activeTab === 'contributions' && (
            <div className="space-y-8">
              <div>
                <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
                  <GitBranch className="w-8 h-8 text-[#7c3aed]" />
                  Contribution Graph
                </h1>
              </div>

              {/* Contribution Heatmap */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8 overflow-x-auto">
                <div className="min-w-[900px]">
                  <div className="flex gap-1">
                    {Array.from({ length: 52 }).map((_, weekIdx) => (
                      <div key={weekIdx} className="flex flex-col gap-1">
                        {Array.from({ length: 7 }).map((_, dayIdx) => {
                          const contribution = contributions[weekIdx * 7 + dayIdx];
                          if (!contribution) return null;

                          return (
                            <div
                              key={dayIdx}
                              className="w-3 h-3 rounded-sm cursor-pointer hover:ring-2 hover:ring-white/50 transition-all group relative"
                              style={{
                                backgroundColor: getContributionColor(contribution.count),
                              }}
                              title={`${contribution.count} contributions on ${contribution.date}`}
                            />
                          );
                        })}
                      </div>
                    ))}
                  </div>

                  {/* Legend */}
                  <div className="flex items-center justify-end gap-2 mt-4 text-xs text-white/40">
                    <span>Less</span>
                    {[0, 3, 6, 9, 12, 15].map((count) => (
                      <div
                        key={count}
                        className="w-3 h-3 rounded-sm"
                        style={{ backgroundColor: getContributionColor(count) }}
                      />
                    ))}
                    <span>More</span>
                  </div>
                </div>
              </div>

              {/* Contribution Stats */}
              <div className="grid grid-cols-4 gap-4">
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6">
                  <div className="flex items-center gap-3 mb-3">
                    <Code2 className="w-5 h-5 text-[#7c3aed]" />
                    <span className="text-sm text-white/60">Total Commits</span>
                  </div>
                  <div className="text-2xl font-bold">1,247</div>
                </div>
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6">
                  <div className="flex items-center gap-3 mb-3">
                    <Target className="w-5 h-5 text-[#0ea5e9]" />
                    <span className="text-sm text-white/60">Busiest Day</span>
                  </div>
                  <div className="text-2xl font-bold">Wednesday</div>
                </div>
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6">
                  <div className="flex items-center gap-3 mb-3">
                    <Zap className="w-5 h-5 text-[#f59e0b]" />
                    <span className="text-sm text-white/60">Avg/Day</span>
                  </div>
                  <div className="text-2xl font-bold">3.4</div>
                </div>
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6">
                  <div className="flex items-center gap-3 mb-3">
                    <Star className="w-5 h-5 text-[#8b5cf6]" />
                    <span className="text-sm text-white/60">Best Streak</span>
                  </div>
                  <div className="text-2xl font-bold">89 days</div>
                </div>
              </div>
            </div>
          )}

          {/* SETTINGS TAB */}
          {activeTab === 'settings' && (
            <div className="space-y-8">
              <div>
                <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
                  <Settings className="w-8 h-8 text-[#7c3aed]" />
                  Account Settings
                </h1>
                <p className="text-white/60">Manage your account and security</p>
              </div>

              {/* Account Information */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Account Information</h2>
                <div className="space-y-4 max-w-2xl">
                  <div>
                    <label className="text-sm text-white/60 mb-2 block">Full Name</label>
                    <input
                      type="text"
                      defaultValue={userData.name}
                      className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50 transition-all"
                    />
                  </div>
                  <div>
                    <label className="text-sm text-white/60 mb-2 block">Email</label>
                    <input
                      type="email"
                      defaultValue={userData.email}
                      className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50 transition-all"
                    />
                  </div>
                  <div>
                    <label className="text-sm text-white/60 mb-2 block">Username</label>
                    <input
                      type="text"
                      defaultValue={userData.username}
                      className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50 transition-all"
                    />
                  </div>
                  <div>
                    <label className="text-sm text-white/60 mb-2 block">Bio</label>
                    <textarea
                      defaultValue={userData.bio}
                      rows={3}
                      className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50 transition-all resize-none"
                    />
                  </div>
                  <button className="px-6 py-3 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-xl hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all">
                    Save Changes
                  </button>
                </div>
              </div>

              {/* Security */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Security</h2>
                <div className="space-y-4 max-w-2xl">
                  <button className="w-full flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl hover:bg-white/10 transition-all">
                    <div className="flex items-center gap-3">
                      <Lock className="w-5 h-5 text-white/60" />
                      <div className="text-left">
                        <div className="font-medium">Change Password</div>
                        <div className="text-sm text-white/40">Last changed 3 months ago</div>
                      </div>
                    </div>
                    <ChevronRight className="w-5 h-5 text-white/40" />
                  </button>

                  <button className="w-full flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl hover:bg-white/10 transition-all">
                    <div className="flex items-center gap-3">
                      <Shield className="w-5 h-5 text-white/60" />
                      <div className="text-left">
                        <div className="font-medium">Two-Factor Authentication</div>
                        <div className="text-sm text-green-400">Enabled</div>
                      </div>
                    </div>
                    <ChevronRight className="w-5 h-5 text-white/40" />
                  </button>
                </div>
              </div>

              {/* Connected Accounts */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Connected Accounts</h2>
                <div className="space-y-3 max-w-2xl">
                  <div className="flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl">
                    <div className="flex items-center gap-3">
                      <Github className="w-5 h-5" />
                      <span>GitHub</span>
                    </div>
                    <button className="px-4 py-2 bg-green-500/20 border border-green-500/30 rounded-lg text-sm text-green-400">
                      Connected
                    </button>
                  </div>
                  <div className="flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl">
                    <div className="flex items-center gap-3">
                      <Chrome className="w-5 h-5" />
                      <span>Google</span>
                    </div>
                    <button className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg-white/10 transition-all">
                      Connect
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* PREFERENCES TAB */}
          {activeTab === 'preferences' && (
            <div className="space-y-8">
              <div>
                <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
                  <Palette className="w-8 h-8 text-[#7c3aed]" />
                  Preferences
                </h1>
                <p className="text-white/60">Customize your CodeAstra experience</p>
              </div>

              {/* Editor Preferences */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Editor Settings</h2>
                <div className="space-y-6 max-w-2xl">
                  <div>
                    <label className="text-sm text-white/60 mb-3 block">Theme</label>
                    <div className="grid grid-cols-3 gap-3">
                      {['Dark', 'Neon', 'Cosmic'].map((theme) => (
                        <button
                          key={theme}
                          className={`p-4 rounded-xl border transition-all ${
                            theme === 'Dark'
                              ? 'bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border-[#7c3aed]/50'
                              : 'bg-white/5 border-white/10 hover:border-white/30'
                          }`}
                        >
                          {theme}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <label className="text-sm text-white/60 mb-3 block">Font Family</label>
                    <select className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50">
                      <option>JetBrains Mono</option>
                      <option>Fira Code</option>
                      <option>Cascadia Code</option>
                      <option>Monaco</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-sm text-white/60 mb-3 block flex items-center justify-between">
                      <span>Font Size</span>
                      <span className="text-white">14px</span>
                    </label>
                    <input type="range" min="10" max="24" defaultValue="14" className="w-full accent-[#7c3aed]" />
                  </div>

                  <div>
                    <label className="text-sm text-white/60 mb-3 block flex items-center justify-between">
                      <span>Tab Size</span>
                      <span className="text-white">2 spaces</span>
                    </label>
                    <div className="flex gap-2">
                      {[2, 4, 8].map((size) => (
                        <button
                          key={size}
                          className={`flex-1 py-2 rounded-lg border transition-all ${
                            size === 2
                              ? 'bg-[#7c3aed]/20 border-[#7c3aed]/50 text-white'
                              : 'bg-white/5 border-white/10 text-white/60 hover:border-white/30'
                          }`}
                        >
                          {size}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Voice & Audio */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Voice & Audio</h2>
                <div className="space-y-6 max-w-2xl">
                  <div>
                    <label className="text-sm text-white/60 mb-3 block">Input Device</label>
                    <select className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50">
                      <option>Default Microphone</option>
                      <option>Built-in Microphone</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-sm text-white/60 mb-3 block">Output Device</label>
                    <select className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#7c3aed]/50">
                      <option>Default Speakers</option>
                      <option>Built-in Speakers</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-sm text-white/60 mb-3 block flex items-center justify-between">
                      <span>Input Volume</span>
                      <span className="text-white">75%</span>
                    </label>
                    <input type="range" min="0" max="100" defaultValue="75" className="w-full accent-[#7c3aed]" />
                  </div>

                  <button className="px-6 py-3 bg-white/5 border border-white/10 rounded-xl hover:bg-white/10 transition-all flex items-center gap-2">
                    <Volume2 className="w-4 h-4" />
                    Test Audio
                  </button>
                </div>
              </div>

              {/* Notifications */}
              <div className="bg-[#0f0f0f] border border-white/5 rounded-3xl p-8">
                <h2 className="text-xl font-semibold mb-6">Notifications</h2>
                <div className="space-y-4 max-w-2xl">
                  {[
                    { label: 'Email Notifications', desc: 'Receive updates via email' },
                    { label: 'Push Notifications', desc: 'Browser notifications' },
                    { label: 'Streak Reminders', desc: 'Daily coding streak alerts' },
                    { label: 'Team Updates', desc: 'Workspace and team activity' },
                  ].map((setting) => (
                    <div
                      key={setting.label}
                      className="flex items-center justify-between p-4 bg-white/5 border border-white/10 rounded-xl"
                    >
                      <div>
                        <div className="font-medium mb-1">{setting.label}</div>
                        <div className="text-sm text-white/40">{setting.desc}</div>
                      </div>
                      <button className="w-12 h-6 bg-[#7c3aed] rounded-full relative transition-all">
                        <div className="w-5 h-5 bg-white rounded-full absolute right-0.5 top-0.5 transition-all" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
