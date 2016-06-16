package org.briarproject.sharing;

import org.briarproject.api.FormatException;
import org.briarproject.api.clients.SessionId;
import org.briarproject.api.contact.ContactId;
import org.briarproject.api.data.BdfDictionary;
import org.briarproject.api.sync.GroupId;
import org.briarproject.api.sync.MessageId;

import static org.briarproject.api.sharing.SharingConstants.CONTACT_ID;
import static org.briarproject.api.sharing.SharingConstants.GROUP_ID;
import static org.briarproject.api.sharing.SharingConstants.IS_SHARER;
import static org.briarproject.api.sharing.SharingConstants.SESSION_ID;
import static org.briarproject.api.sharing.SharingConstants.SHAREABLE_ID;
import static org.briarproject.api.sharing.SharingConstants.STATE;
import static org.briarproject.api.sharing.SharingConstants.STORAGE_ID;

// This class is not thread-safe
public abstract class SharingSessionState {

	private final SessionId sessionId;
	private final MessageId storageId;
	private final GroupId groupId;
	private final ContactId contactId;
	private final GroupId shareableId;
	private int task = -1; // TODO get rid of task, see #376

	public SharingSessionState(SessionId sessionId, MessageId storageId,
			GroupId groupId, ContactId contactId, GroupId shareableId) {

		this.sessionId = sessionId;
		this.storageId = storageId;
		this.groupId = groupId;
		this.contactId = contactId;
		this.shareableId = shareableId;
	}

	public static SharingSessionState fromBdfDictionary(
			InviteeSessionStateFactory isFactory,
			SharerSessionStateFactory ssFactory, BdfDictionary d)
			throws FormatException {

		SessionId sessionId = new SessionId(d.getRaw(SESSION_ID));
		MessageId messageId = new MessageId(d.getRaw(STORAGE_ID));
		GroupId groupId = new GroupId(d.getRaw(GROUP_ID));
		ContactId contactId = new ContactId(d.getLong(CONTACT_ID).intValue());
		GroupId forumId = new GroupId(d.getRaw(SHAREABLE_ID));

		int intState = d.getLong(STATE).intValue();
		if (d.getBoolean(IS_SHARER)) {
			SharerSessionState.State state =
					SharerSessionState.State.fromValue(intState);
			return ssFactory.build(sessionId, messageId, groupId, state,
					contactId, forumId, d);
		} else {
			InviteeSessionState.State state =
					InviteeSessionState.State.fromValue(intState);
			return isFactory.build(sessionId, messageId, groupId, state,
					contactId, forumId, d);
		}
	}

	public BdfDictionary toBdfDictionary() {
		BdfDictionary d = new BdfDictionary();
		d.put(SESSION_ID, getSessionId());
		d.put(STORAGE_ID, getStorageId());
		d.put(GROUP_ID, getGroupId());
		d.put(CONTACT_ID, getContactId().getInt());
		d.put(SHAREABLE_ID, getShareableId());

		return d;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public MessageId getStorageId() {
		return storageId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public GroupId getShareableId() {
		return shareableId;
	}

	public void setTask(int task) {
		this.task = task;
	}

	public int getTask() {
		return task;
	}

}