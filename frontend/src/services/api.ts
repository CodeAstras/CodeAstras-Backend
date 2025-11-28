export async function createProject() {
    const response = await fetch("http://localhost:8080/api/project/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" }
    });

    if (!response.ok) {
        throw new Error("Failed to create project");
    }

    return response.json();
}
