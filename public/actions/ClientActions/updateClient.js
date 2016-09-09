export function updateClient(id, client) {
    return {
        type:       'CLIENT_UPDATE_REQUEST',
        id:         id,
        client:     client,
        receivedAt: Date.now()
    };
}
