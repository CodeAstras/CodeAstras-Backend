        r.setId(room.getId());
        r.setName(room.getName());
        r.setCreatedBy(room.getCreatedBy().getId());
        r.setCreatedAt(room.getCreatedAt().toInstant());
        r.setIsActive(room.isActive());
        r.setMembers(
