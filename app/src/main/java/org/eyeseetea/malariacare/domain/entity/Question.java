package org.eyeseetea.malariacare.domain.entity;

public class Question {
    private long id;
    private boolean cachedVisibility;
    private boolean isCompulsory;
    private boolean removed;

    public Question() {
    }

    public Question(long id, boolean isCompulsory) {
        this.id = id;
        this.isCompulsory = isCompulsory;
    }

    public Question(long id, boolean isCompulsory,  boolean cachedVisibility) {
        this.id = id;
        this.cachedVisibility = cachedVisibility;
        this.isCompulsory = isCompulsory;
    }

    public long getId() {
        return id;
    }

    public boolean isCachedVisibility() {
        return cachedVisibility;
    }

    public boolean isCompulsory() {
        return isCompulsory;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (id != question.id) return false;
        if (cachedVisibility != question.cachedVisibility) return false;
        if (removed != question.removed) return false;
        return isCompulsory == question.isCompulsory;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (cachedVisibility ? 1 : 0);
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", cachedVisibility=" + cachedVisibility +
                ", isCompulsory=" + isCompulsory +
                ", isremoved=" + removed +
                '}';
    }
}
