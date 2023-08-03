export const getDate = (time: string | undefined) => {
    if (time === undefined) {
        return '--/--/----'
    }
    var date = new Date(Number(time))
    var year = date.getFullYear()
    var month = date.getMonth()
    var day = date.getDay()
    var hours = date.getHours()
    var minutes = date.getMinutes()
    var seconds = date.getSeconds()
    return `${day}/${month}/${year}`
}

export type Collection = {
    pageIndex: number,
    pageSize: number,
    collectionSize: number,
}