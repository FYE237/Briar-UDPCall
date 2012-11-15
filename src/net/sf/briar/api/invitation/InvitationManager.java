package net.sf.briar.api.invitation;

/** Creates and manages tasks for exchanging invitations with remote peers. */
public interface InvitationManager {

	/** Creates a task using the given invitation codes. */
	InvitationTask createTask(int localCode, int remoteCode);

	/**
	 * Returns the previously created task with the given handle, unless the
	 * task has subsequently removed itself.
	 */
	InvitationTask getTask(int handle);

	/** Called by tasks to remove themselves when they terminate. */
	void removeTask(int handle);
}