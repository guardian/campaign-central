export const SET_TERRITORY = 'SET_TERRITORY';

export function setTerritory(territory) {
    return {
        type:       SET_TERRITORY,
        territory:  territory,
        receivedAt: Date.now()
    };
}
