import { useState } from 'react';
import {
  Code2,
  Users,
  Folder,
  ListTodo,
  FileText,
  Settings,
  Search,
  Bell,
  Plus,
  UserPlus,
  GitBranch,
  Clock,
  Play,
  CheckCircle2,
  Circle,
  AlertCircle,
  Crown,
  Shield,
  Eye,
  Edit3,
  MoreVertical,
  Terminal,
  FileCode,
  ChevronRight,
  MessageSquare,
  Video,
  Zap,
  Upload,
  Download,
  Star,
  TrendingUp,
  Activity,
  Database,
} from 'lucide-react';
import { CosmicStars } from "../components/workspace/CosmicStars";

import { useNavigate } from 'react-router-dom';

type TabView = 'overview' | 'code' | 'files' | 'tasks' | 'members' | 'docs' | 'settings';

export default function TeamWorkspace() {
  const [activeTab, setActiveTab] = useState<TabView>('overview');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const navigate = useNavigate();

  // Mock data
  const teamInfo = {
    name: 'Frontend Squad',
    logo: 'FS',
    description: 'Building next-generation user interfaces with React, TypeScript, and modern tools',
    lead: 'Alex Chen',
    memberCount: 12,
    projectCount: 5,
  };

  const projects = [
    {
      id: 1,
      name: 'E-Commerce Platform',
      tech: ['React', 'TypeScript', 'Tailwind'],
      lastUpdated: '2 hours ago',
      activeMembers: 4,
      status: 'active',
      progress: 65,
      color: '#7c3aed',
    },
    {
      id: 2,
      name: 'Mobile App Backend',
      tech: ['Node.js', 'MongoDB', 'Express'],
      lastUpdated: '5 hours ago',
      activeMembers: 2,
      status: 'active',
      progress: 40,
      color: '#0ea5e9',
    },
    {
      id: 3,
      name: 'Analytics Dashboard',
      tech: ['Python', 'FastAPI', 'PostgreSQL'],
      lastUpdated: '1 day ago',
      activeMembers: 3,
      status: 'review',
      progress: 85,
      color: '#06b6d4',
    },
  ];

  const activities = [
    { id: 1, type: 'commit', user: 'Sarah Kim', action: 'pushed 3 commits to', target: 'main branch', time: '5m ago', color: '#7c3aed' },
    { id: 2, type: 'file', user: 'Mike Ross', action: 'updated', target: 'package.json', time: '12m ago', color: '#0ea5e9' },
    { id: 3, type: 'room', user: 'Emma Wilson', action: 'joined voice room', target: 'Frontend Sprint', time: '25m ago', color: '#06b6d4' },
    { id: 4, type: 'member', user: 'John Doe', action: 'was added to', target: 'the team', time: '1h ago', color: '#8b5cf6' },
    { id: 5, type: 'commit', user: 'Alex Chen', action: 'merged PR #42', target: 'Feature/auth', time: '2h ago', color: '#ec4899' },
  ];

  const members = [
    { id: 1, name: 'Alex Chen', role: 'Owner', avatar: 'AC', status: 'online', color: '#7c3aed', lastActive: 'now' },
    { id: 2, name: 'Sarah Kim', role: 'Admin', avatar: 'SK', status: 'online', color: '#0ea5e9', lastActive: 'now' },
    { id: 3, name: 'Mike Ross', role: 'Editor', avatar: 'MR', status: 'online', color: '#06b6d4', lastActive: '5m ago' },
    { id: 4, name: 'Emma Wilson', role: 'Editor', avatar: 'EW', status: 'online', color: '#8b5cf6', lastActive: 'now' },
    { id: 5, name: 'John Doe', role: 'Editor', avatar: 'JD', status: 'offline', color: '#ec4899', lastActive: '2d ago' },
    { id: 6, name: 'Jane Smith', role: 'Viewer', avatar: 'JS', status: 'offline', color: '#f59e0b', lastActive: '1w ago' },
  ];

  const tasks = [
    { id: 1, title: 'Implement user authentication', status: 'todo', priority: 'high', assignees: ['AC', 'SK'], tags: ['backend', 'security'] },
    { id: 2, title: 'Design new dashboard layout', status: 'in-progress', priority: 'medium', assignees: ['EW'], tags: ['design', 'ui'] },
    { id: 3, title: 'Fix mobile responsive issues', status: 'in-progress', priority: 'high', assignees: ['MR', 'JD'], tags: ['frontend', 'bug'] },
    { id: 4, title: 'Write API documentation', status: 'review', priority: 'low', assignees: ['AC'], tags: ['docs'] },
    { id: 5, title: 'Optimize database queries', status: 'done', priority: 'medium', assignees: ['SK'], tags: ['backend', 'performance'] },
  ];

  const files = [
    { id: 1, name: 'src', type: 'folder', size: '-', modified: '1h ago', icon: Folder },
    { id: 2, name: 'components', type: 'folder', size: '-', modified: '2h ago', icon: Folder },
    { id: 3, name: 'package.json', type: 'file', size: '2.4 KB', modified: '12m ago', icon: FileCode },
    { id: 4, name: 'README.md', type: 'file', size: '8.1 KB', modified: '1d ago', icon: FileText },
    { id: 5, name: 'tsconfig.json', type: 'file', size: '1.2 KB', modified: '3d ago', icon: FileCode },
  ];

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'Owner':
        return Crown;
      case 'Admin':
        return Shield;
      case 'Editor':
        return Edit3;
      case 'Viewer':
        return Eye;
      default:
        return Users;
    }
  };

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'Owner':
        return '#f59e0b';
      case 'Admin':
        return '#7c3aed';
      case 'Editor':
        return '#0ea5e9';
      case 'Viewer':
        return '#6b7280';
      default:
        return '#ffffff';
    }
  };

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
          {/* Left - Team Info */}
          <div className="flex items-center gap-4">
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

            <div className="w-px h-6 bg-white/10" />

            <div className="flex items-center gap-3">
              <div
                className="w-9 h-9 rounded-lg flex items-center justify-center font-semibold text-sm"
                style={{ backgroundColor: '#7c3aed30', color: '#7c3aed' }}
              >
                {teamInfo.logo}
              </div>
              <div>
                <h1 className="font-semibold">{teamInfo.name}</h1>
                <p className="text-xs text-white/40">{teamInfo.memberCount} members · {teamInfo.projectCount} projects</p>
              </div>
            </div>
          </div>

          {/* Center - Navigation Tabs */}
          <nav className="flex items-center gap-1">
            {[
              { id: 'overview', label: 'Overview', icon: TrendingUp },
              { id: 'code', label: 'Code Workspace', icon: Code2 },
              { id: 'files', label: 'Files', icon: Folder },
              { id: 'tasks', label: 'Tasks', icon: ListTodo },
              { id: 'members', label: 'Members', icon: Users },
              { id: 'docs', label: 'Docs', icon: FileText },
              { id: 'settings', label: 'Settings', icon: Settings },
            ].map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as TabView)}
                  className={`relative px-4 py-2 rounded-lg transition-all duration-300 flex items-center gap-2 ${
                    activeTab === tab.id
                      ? 'text-white'
                      : 'text-white/60 hover:text-white/90 hover:bg:white/5'
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
          </nav>

          {/* Right - Actions */}
          <div className="flex items-center gap-3">
            <div className="relative">
              <input
                type="text"
                placeholder="Search..."
                className="w-48 bg-white/5 border border-white/10 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:border-[#7c3aed]/50 placeholder:text-white/30"
              />
              <Search className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
            </div>
            <button className="relative p-2 rounded-lg hover:bg-white/5 transition-colors">
              <Bell className="w-5 h-5 text-white/60" />
              <div className="absolute top-1.5 right-1.5 w-2 h-2 bg-[#0ea5e9] rounded-full animate-pulse" />
            </button>
            <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center font-semibold cursor-pointer hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all">
              YO
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex pt-16 h-screen">
        {/* Optional Sidebar */}
        {sidebarOpen && (
          <aside className="w-64 bg-[#0f0f0f] border-r border-white/5 p-4 overflow-y-auto">
            <div className="space-y-6">
              {/* Quick Actions */}
              <div>
                <h3 className="text-xs uppercase text-white/40 mb-3 px-2">Quick Actions</h3>
                <div className="space-y-1">
                  <button className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-white/5 transition-all text-sm text-white/80">
                    <Plus className="w-4 h-4" />
                    New Project
                  </button>
                  <button className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg:white/5 transition-all text-sm text-white/80">
                    <UserPlus className="w-4 h-4" />
                    Invite Member
                  </button>
                  <button 
                    onClick={() => navigate('/room')}
                    className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg:white/5 transition-all text-sm text-white/80"
                  >
                    <Video className="w-4 h-4" />
                    Start Room
                  </button>
                </div>
              </div>

              {/* Projects List */}
              <div>
                <h3 className="text-xs uppercase text-white/40 mb-3 px-2">Projects</h3>
                <div className="space-y-1">
                  {projects.map((project) => (
                    <button
                      key={project.id}
                      className="w-full flex items-center justify-between px-3 py-2 rounded-lg hover:bg:white/5 transition-all text-sm group"
                    >
                      <div className="flex items-center gap-2">
                        <div
                          className="w-2 h-2 rounded-full"
                          style={{ backgroundColor: project.color }}
                        />
                        <span className="text-white/80 truncate">{project.name}</span>
                      </div>
                      <ChevronRight className="w-3 h-3 text-white/40 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </button>
                  ))}
                </div>
              </div>

              {/* Team Voice Channel */}
              <div>
                <h3 className="text-xs uppercase text-white/40 mb-3 px-2">Voice Channel</h3>
                <div className="bg-white/5 border border-white/10 rounded-xl p-3">
                  <div className="flex items-center gap-2 mb-2">
                    <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                    <span className="text-xs text-green-400">3 members active</span>
                  </div>
                  <button 
                    onClick={() => navigate('/room')}
                    className="w-full px-3 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm font-medium hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all"
                  >
                    Join Channel
                  </button>
                </div>
              </div>
            </div>
          </aside>
        )}

        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-6 relative z-10">
          {/* OVERVIEW TAB */}
          {activeTab === 'overview' && (
            <div className="grid grid-cols-12 gap-6">
              {/* Left Column - Team Info */}
              <div className="col-span-12 lg:col-span-3">
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 space-y-6">
                  <div>
                    <h2 className="text-lg font-semibold mb-2">Team Info</h2>
                    <p className="text-sm text-white/60">{teamInfo.description}</p>
                  </div>

                  <div>
                    <div className="text-xs uppercase text-white/40 mb-2">Team Lead</div>
                    <div className="flex items-center gap-2">
                      <Crown className="w-4 h-4 text-yellow-500" />
                      <span className="text-sm">{teamInfo.lead}</span>
                    </div>
                  </div>

                  <div>
                    <div className="text-xs uppercase text-white/40 mb-2">Stats</div>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-white/60">Members</span>
                        <span>{teamInfo.memberCount}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-white/60">Projects</span>
                        <span>{teamInfo.projectCount}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-white/60">Active Now</span>
                        <span className="text-green-400">4</span>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <button className="w-full px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm font-medium hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center justify-center gap-2">
                      <UserPlus className="w-4 h-4" />
                      Invite Member
                    </button>
                    <button className="w-full px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg:white/10 transition-all flex items-center justify-center gap-2">
                      <Plus className="w-4 h-4" />
                      Create Project
                    </button>
                  </div>
                </div>
              </div>

              {/* Center Column - Project Spaces */}
              <div className="col-span-12 lg:col-span-6">
                <div className="mb-6">
                  <h2 className="text-2xl font-semibold flex items-center gap-3 mb-4">
                    <Folder className="w-6 h-6 text-[#7c3aed]" />
                    Project Spaces
                  </h2>
                </div>

                <div className="space-y-4">
                  {projects.map((project) => (
                    <div
                      key={project.id}
                      className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6 hover:border-[#7c3aed]/30 transition-all duration-300 group"
                    >
                      <div className="flex items-start justify-between mb-4">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <h3 className="text-lg font-semibold">{project.name}</h3>
                            <div
                              className={`px-2 py-1 rounded-full text-xs font-medium ${
                                project.status === 'active'
                                  ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                                  : 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                              }`}
                            >
                              {project.status}
                            </div>
                          </div>
                          <div className="flex items-center gap-2 flex-wrap">
                            {project.tech.map((tech) => (
                              <span
                                key={tech}
                                className="px-2 py-1 bg:white/5 border border-white/10 rounded text-xs text-white/70"
                              >
                                {tech}
                              </span>
                            ))}
                          </div>
                        </div>
                        <div
                          className="w-3 h-3 rounded-full"
                          style={{ backgroundColor: project.color, boxShadow: `0 0 10px ${project.color}80` }}
                        />
                      </div>

                      {/* Progress Bar */}
                      <div className="mb-4">
                        <div className="flex justify-between text-xs text-white/40 mb-2">
                          <span>Progress</span>
                          <span>{project.progress}%</span>
                        </div>
                        <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-full transition-all"
                            style={{ width: `${project.progress}%` }}
                          />
                        </div>
                      </div>

                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4 text-sm text-white/60">
                          <div className="flex items-center gap-1">
                            <Clock className="w-4 h-4" />
                            {project.lastUpdated}
                          </div>
                          <div className="flex items-center gap-1">
                            <Users className="w-4 h-4" />
                            {project.activeMembers} active
                          </div>
                        </div>
                        <button 
                          onClick={() => navigate('/workspace')}
                          className="px-4 py-2 bg-gradient-to-r from-[#7c3aed]/20 to-[#0ea5e9]/20 border border-[#7c3aed]/30 rounded-lg text-sm hover:from-[#7c3aed]/30 hover:to-[#0ea5e9]/30 transition-all"
                        >
                          Open Project
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Right Column - Activity Feed */}
              <div className="col-span-12 lg:col-span-3">
                <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-6">
                  <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <Activity className="w-5 h-5 text-[#0ea5e9]" />
                    Activity Feed
                  </h2>

                  <div className="space-y-4">
                    {activities.map((activity, idx) => (
                      <div key={activity.id} className="relative pl-6">
                        {/* Timeline dot and line */}
                        <div
                          className="absolute left-0 top-1.5 w-2 h-2 rounded-full"
                          style={{
                            backgroundColor: activity.color,
                            boxShadow: `0 0 8px ${activity.color}80`,
                          }}
                        />
                        {idx < activities.length - 1 && (
                          <div className="absolute left-[3px] top-3 w-px h-full bg-gradient-to-b from-white/20 to-transparent" />
                        )}

                        <div className="text-sm">
                          <span className="font-medium">{activity.user}</span>
                          <span className="text-white/60"> {activity.action} </span>
                          <span className="text-white/80">{activity.target}</span>
                          <div className="text-xs text-white/40 mt-1">{activity.time}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* CODE WORKSPACE TAB */}
          {activeTab === 'code' && (
            <div className="h-full">
              <div className="bg-[#0f0f0f] border border-white/5 rounded-2xl p-8 flex items-center justify-center h-[600px]">
                <div className="text-center">
                  <Code2 className="w-16 h-16 text:white/20 mx-auto mb-4" />
                  <h3 className="text-xl font-semibold mb-2">Team Code Workspace</h3>
                  <p className="text-white/40 mb-6 max-w-md">
                    Full-featured IDE with real-time collaboration, version control, and terminal access
                  </p>
                  <button 
                    onClick={() => navigate('/workspace')}
                    className="px-6 py-3 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-xl hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all"
                  >
                    Open Workspace
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* FILES TAB */}
          {activeTab === 'files' && (
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-semibold flex items-center gap-3">
                  <Folder className="w-6 h-6 text-[#7c3aed]" />
                  Team Files
                </h2>
                <div className="flex items-center gap-2">
                  <button className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm hover:bg:white/10 transition-all flex items-center gap-2">
                    <Upload className="w-4 h-4" />
                    Upload
                  </button>
                  <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                    <Plus className="w-4 h-4" />
                    New File
                  </button>
                </div>
              </div>

              <div className="bg-[#0f0f0f] border border:white/5 rounded-2xl overflow-hidden">
                {/* File table header */}
                <div className="grid grid-cols-12 gap-4 px-6 py-3 border-b border-white/5 text-xs uppercase text-white/40">
                  <div className="col-span-6">Name</div>
                  <div className="col-span-2">Size</div>
                  <div className="col-span-3">Modified</div>
                  <div className="col-span-1">Actions</div>
                </div>

                {/* File list */}
                {files.map((file) => {
                  const Icon = file.icon;
                  return (
                    <div
                      key={file.id}
                      className="grid grid-cols-12 gap-4 px-6 py-4 border-b border-white/5 hover:bg:white/5 transition-all cursor-pointer group"
                    >
                      <div className="col-span-6 flex items-center gap-3">
                        <Icon className="w-5 h-5 text:white/40" />
                        <span className="text-sm">{file.name}</span>
                      </div>
                      <div className="col-span-2 text-sm text-white/60">{file.size}</div>
                      <div className="col-span-3 text-sm text:white/60">{file.modified}</div>
                      <div className="col-span-1">
                        <button className="opacity-0 group-hover:opacity-100 p-1 hover:bg:white/10 rounded transition-all">
                          <MoreVertical className="w-4 h-4 text:white/60" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* TASKS TAB - Kanban Board */}
          {activeTab === 'tasks' && (
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-semibold flex items-center gap-3">
                  <ListTodo className="w-6 h-6 text-[#7c3aed]" />
                  Task Board
                </h2>
                <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                  <Plus className="w-4 h-4" />
                  New Task
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {['todo', 'in-progress', 'review', 'done'].map((status) => {
                  const statusTasks = tasks.filter((t) => t.status === status);
                  return (
                    <div key={status} className="bg-[#0f0f0f] border border:white/5 rounded-2xl p-4">
                      <div className="flex items-center justify-between mb-4">
                        <h3 className="font-semibold text-sm uppercase text:white/80">
                          {status === 'in-progress' ? 'In Progress' : status.replace('-', ' ')}
                        </h3>
                        <div className="px-2 py-1 bg:white/5 rounded-full text-xs">{statusTasks.length}</div>
                      </div>

                      <div className="space-y-3">
                        {statusTasks.map((task) => (
                          <div
                            key={task.id}
                            className="bg:white/5 border border:white/10 rounded-xl p-4 hover:bg:white/10 transition-all cursor-pointer group"
                          >
                            <div className="flex items-start justify-between mb-3">
                              <p className="text-sm font-medium flex-1">{task.title}</p>
                              {task.priority === 'high' ? (
                                <AlertCircle className="w-4 h-4 text-red-400 flex-shrink-0" />
                              ) : task.priority === 'medium' ? (
                                <Circle className="w-4 h-4 text-yellow-400 flex-shrink-0" />
                              ) : (
                                <Circle className="w-4 h-4 text-green-400 flex-shrink-0" />
                              )}
                            </div>

                            <div className="flex items-center gap-2 flex-wrap mb-3">
                              {task.tags.map((tag) => (
                                <span
                                  key={tag}
                                  className="px-2 py-0.5 bg-[#7c3aed]/20 border border-[#7c3aed]/30 rounded text-xs text-[#7c3aed]"
                                >
                                  {tag}
                                </span>
                              ))}
                            </div>

                            <div className="flex -space-x-2">
                              {task.assignees.map((assignee, idx) => (
                                <div
                                  key={idx}
                                  className="w-6 h-6 rounded-full bg-gradient-to-br from-[#7c3aed] to-[#0ea5e9] flex items-center justify-center text-xs font-semibold border-2 border-[#0f0f0f]"
                                >
                                  {assignee}
                                </div>
                              ))}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* MEMBERS TAB */}
          {activeTab === 'members' && (
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-semibold flex items-center gap-3">
                  <Users className="w-6 h-6 text-[#7c3aed]" />
                  Team Members
                </h2>
                <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                  <UserPlus className="w-4 h-4" />
                  Invite Member
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {members.map((member) => {
                  const RoleIcon = getRoleIcon(member.role);
                  const roleColor = getRoleColor(member.role);
                  return (
                    <div
                      key={member.id}
                      className="bg-[#0f0f0f] border border:white/5 rounded-2xl p-6 hover:border:white/10 transition-all"
                    >
                      <div className="flex items-start gap-4 mb-4">
                        <div className="relative">
                          <div
                            className="w-14 h-14 rounded-xl flex items-center justify-center font-semibold"
                            style={{ backgroundColor: `${member.color}20`, color: member.color }}
                          >
                            {member.avatar}
                          </div>
                          <div
                            className={`absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-[#0f0f0f] ${
                              member.status === 'online' ? 'bg-green-500' : 'bg-white/20'
                            }`}
                          />
                        </div>
                        <div className="flex-1">
                          <h3 className="font-semibold mb-1">{member.name}</h3>
                          <div className="flex items-center gap-2 text-xs">
                            <RoleIcon className="w-3 h-3" style={{ color: roleColor }} />
                            <span style={{ color: roleColor }}>{member.role}</span>
                          </div>
                        </div>
                      </div>

                      <div className="text-xs text-white/40 mb-4">
                        Last active: {member.lastActive}
                      </div>

                      <div className="flex gap-2">
                        <button className="flex-1 px-3 py-2 bg:white/5 border border:white/10 rounded-lg text-xs hover:bg:white/10 transition-all">
                          <MessageSquare className="w-3 h-3 inline mr-1" />
                          Message
                        </button>
                        <button className="px-3 py-2 bg:white/5 border border:white/10 rounded-lg text-xs hover:bg:white/10 transition-all">
                          <MoreVertical className="w-3 h-3" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* DOCS TAB */}
          {activeTab === 'docs' && (
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-semibold flex items-center gap-3">
                  <FileText className="w-6 h-6 text-[#7c3aed]" />
                  Team Documentation
                </h2>
                <button className="px-4 py-2 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-lg text-sm hover:shadow-lg hover:shadow-[#7c3aed]/30 transition-all flex items-center gap-2">
                  <Plus className="w-4 h-4" />
                  New Document
                </button>
              </div>

              <div className="bg-[#0f0f0f] border border:white/5 rounded-2xl p-8">
                <div className="max-w-3xl">
                  <h3 className="text-xl font-semibold mb-4">📚 Getting Started</h3>
                  <div className="space-y-4 text-white/70">
                    <p>
                      Welcome to the Frontend Squad workspace. This is your team's central hub for
                      documentation, notes, and knowledge sharing.
                    </p>
                    <h4 className="text-lg font-semibold text-white mt-6 mb-2">Architecture Overview</h4>
                    <p>Our application uses a modern React + TypeScript stack with:</p>
                    <ul className="list-disc list-inside space-y-2 ml-4">
                      <li>React 18 with hooks and context API</li>
                      <li>TypeScript for type safety</li>
                      <li>Tailwind CSS for styling</li>
                      <li>Vite as build tool</li>
                    </ul>
                    <h4 className="text-lg font-semibold text-white mt-6 mb-2">API Reference</h4>
                    <div className="bg-[#1a1a1a] border border-white/10 rounded-lg p-4 font-mono text-sm">
                      <div className="text-[#7c3aed]">POST</div>
                      <div className="text-[#0ea5e9]">/api/v1/users</div>
                      <div className="text-white/60 mt-2">Create a new user account</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* SETTINGS TAB */}
          {activeTab === 'settings' && (
            <div>
              <div className="mb-6">
                <h2 className="text-2xl font-semibold flex items-center gap-3">
                  <Settings className="w-6 h-6 text-[#7c3aed]" />
                  Team Settings
                </h2>
              </div>

              <div className="space-y-6 max-w-2xl">
                <div className="bg-[#0f0f0f] border border:white/5 rounded-2xl p-6">
                  <h3 className="font-semibold mb-4">General Settings</h3>
                  <div className="space-y-4">
                    <div>
                      <label className="text-sm text:white/60 mb-2 block">Team Name</label>
                      <input
                        type="text"
                        value={teamInfo.name}
                        className="w-full bg:white/5 border border:white/10 rounded-lg px-4 py-2 focus:outline-none focus:border-[#7c3aed]/50"
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="text-sm text:white/60 mb-2 block">Description</label>
                      <textarea
                        value={teamInfo.description}
                        rows={3}
                        className="w-full bg:white/5 border border:white/10 rounded-lg px-4 py-2 focus:outline-none focus:border-[#7c3aed]/50 resize-none"
                        readOnly
                      />
                    </div>
                  </div>
                </div>

                <div className="bg-[#0f0f0f] border border:white/5 rounded-2xl p-6">
                  <h3 className="font-semibold mb-4">Permissions</h3>
                  <div className="space-y-3">
                    {['Owner', 'Admin', 'Editor', 'Viewer'].map((role) => (
                      <div key={role} className="flex items-center justify-between py-2">
                        <div className="flex items-center gap-3">
                          {(() => {
                            const Icon = getRoleIcon(role);
                            return <Icon className="w-5 h-5" style={{ color: getRoleColor(role) }} />;
                          })()}
                          <span>{role}</span>
                        </div>
                        <button className="text-sm text:white/60 hover:text:white">Configure</button>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="bg-[#0f0f0f] border border-red-500/20 rounded-2xl p-6">
                  <h3 className="font-semibold mb-2 text-red-400">Danger Zone</h3>
                  <p className="text-sm text:white/60 mb-4">
                    Deleting this workspace is permanent and cannot be undone.
                  </p>
                  <button className="px-4 py-2 bg-red-500/10 border border-red-500/30 rounded-lg text-sm text-red-400 hover:bg-red-500/20 transition-all">
                    Delete Workspace
                  </button>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
