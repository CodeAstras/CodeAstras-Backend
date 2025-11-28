import { useState, useEffect, useRef } from 'react';
import { IDEHeader } from '../components/workspace/IDEHeader';
import { IntegrationsSidebar } from '../components/workspace/IntegrationsSidebar';
import { FileExplorer } from '../components/workspace/FileExplorer';
import { EditorTabs } from '../components/workspace/EditorTabs';
import { CodeEditor } from '../components/workspace/CodeEditor';
import { Terminal } from '../components/workspace/Terminal';
import { VideoPanel } from '../components/workspace/VideoPanel';
import { ParticipantsList } from '../components/workspace/ParticipantsList';
import { ChatPanel } from '../components/workspace/ChatPanel';
import { CosmicStars } from '../components/workspace/CosmicStars';

export default function Workspace() {
  const [fileExplorerOpen, setFileExplorerOpen] = useState(true);
  const [terminalOpen, setTerminalOpen] = useState(false);
  const [videoMode, setVideoMode] = useState<'video' | 'audio'>('video');
  const [videoPanelOpen, setVideoPanelOpen] = useState(true);
  const [participantsPanelOpen, setParticipantsPanelOpen] = useState(true);

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-white overflow-hidden relative">
      {/* Cosmic background with reduced opacity */}
      <CosmicStars />
      
      {/* Subtle background gradients */}
      <div className="fixed inset-0 pointer-events-none opacity-10">
        <div className="absolute top-0 left-1/4 w-[500px] h-[500px] bg-[#7c3aed] rounded-full blur-[150px]" />
        <div className="absolute bottom-1/4 right-1/4 w-[500px] h-[500px] bg-[#0ea5e9] rounded-full blur-[150px]" />
      </div>

      {/* Header */}
      <IDEHeader 
        visible={true}
        onToggleParticipants={() => setParticipantsPanelOpen(!participantsPanelOpen)}
        onToggleVideo={() => setVideoPanelOpen(!videoPanelOpen)}
        participantsOpen={participantsPanelOpen}
        videoOpen={videoPanelOpen}
      />

      {/* Main workspace container */}
      <div className="flex h-screen pt-14 overflow-hidden">
        {/* Left - IDE Section */}
        <div className="flex flex-1 min-w-0 relative">
          {/* Integrations sidebar */}
          <div className="hidden md:block">
            <IntegrationsSidebar />
          </div>

          {/* File Explorer */}
          {fileExplorerOpen && (
            <div className="hidden md:block">
              <FileExplorer onClose={() => setFileExplorerOpen(false)} />
            </div>
          )}

          {/* Editor area */}
          <div className="flex-1 min-w-0 flex flex-col relative bg-[#0a0a0a]">
            {/* Editor tabs */}
            <EditorTabs />

            {/* Main editor */}
            <div className="flex-1 relative overflow-hidden">
              <CodeEditor />
            </div>

            {/* Terminal panel */}
            <Terminal isOpen={terminalOpen} onToggle={() => setTerminalOpen(!terminalOpen)} />
          </div>
        </div>

        {/* Right - Chat & Participants (collapsible) */}
        <div className={`transition-all duration-300 border-l border-white/5 bg-[#0f0f0f] flex flex-col overflow-hidden ${participantsPanelOpen ? 'w-72' : 'w-0'}`}>
          {/* Participants list */}
          <ParticipantsList />

          {/* Chat panel */}
          <ChatPanel />
        </div>

        {/* Far Right - Video Chat Panel (collapsible) */}
        <div className={`transition-all duration-300 border-l border-white/5 bg-[#0f0f0f] flex flex-col overflow-hidden ${videoPanelOpen ? 'w-96' : 'w-0'}`}>
          <VideoPanel mode={videoMode} onModeChange={setVideoMode} />
        </div>
      </div>

      {/* Mobile toggle buttons */}
      <button
        onClick={() => setTerminalOpen(!terminalOpen)}
        className="lg:hidden fixed bottom-4 left-4 z-40 w-12 h-12 bg-gradient-to-r from-[#7c3aed] to-[#0ea5e9] rounded-full shadow-lg flex items-center justify-center"
      >
        {terminalOpen ? '×' : '$'}
      </button>
    </div>
  );
}