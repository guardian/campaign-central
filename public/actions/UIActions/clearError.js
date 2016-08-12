
export function clearError() {
    return {
        type:       'CLEAR_ERROR',
        receivedAt: Date.now()
    };
}
