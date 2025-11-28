import { BrowserRouter, Routes, Route } from "react-router-dom";

import LandingPage from "./pages/LandingPage";
import Dashboard from "./pages/Dashboard";
import Workspace from "./pages/Workspace";
import MeetingRoom from "./pages/MeetingRoom";
import TeamWorkspace from "./pages/TeamWorkspace";
import Profile from "./pages/Profile";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<LandingPage />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/workspace" element={<Workspace />} />
                <Route path="/room" element={<MeetingRoom />} />
                <Route path="/team" element={<TeamWorkspace />} />
                <Route path="/profile" element={<Profile />} />

                {/* Auth */}
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />

                {/* Dynamic project route */}
                <Route path="/editor/:projectId" element={<Workspace />} />

            </Routes>
        </BrowserRouter>
    );
}
