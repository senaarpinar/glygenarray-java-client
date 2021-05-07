package org.glygen.array.client.model.data;

import java.util.List;

public interface ChangeTrackable {
    public List<ChangeLog> getChanges();
    public void setChanges(List<ChangeLog> changes);
    public void addChange (ChangeLog change);
}
