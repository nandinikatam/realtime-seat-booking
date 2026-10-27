import { useEffect, useState } from 'react'
import { Client } from '@stomp/stompjs'
import './App.css'

const EVENT_ID = 1

export default function App() {
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState(null)
  const [message, setMessage] = useState('')
  const [connected, setConnected] = useState(false)
  const [now, setNow] = useState(Date.now())

  const loadSeats = () =>
      fetch(`/api/events/${EVENT_ID}/seats`)
          .then((r) => r.json())
          .then(setSeats)

  // initial load
  useEffect(() => {
    loadSeats()
  }, [])

  // live updates over WebSocket
  useEffect(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 3000,
    })
    client.onConnect = () => {
      setConnected(true)
      client.subscribe(`/topic/events/${EVENT_ID}/seats`, (msg) => {
        const updated = JSON.parse(msg.body)
        setSeats((prev) => prev.map((s) => (s.id === updated.id ? updated : s)))
      })
      loadSeats() // catch up on anything missed while offline
    }
    client.onWebSocketClose = () => setConnected(false)
    client.activate()
    return () => client.deactivate()
  }, [])

  // ticking clock for the hold countdown
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(t)
  }, [])

  // if my held seat stops being HELD (expired), clear it
  useEffect(() => {
    if (!selected) return
    const current = seats.find((s) => s.id === selected.id)
    if (current && current.status !== 'HELD') {
      setMessage('Your hold expired. Please pick a seat again.')
      setSelected(null)
    }
  }, [seats, selected])

  async function post(path) {
    const res = await fetch(path, { method: 'POST' })
    const body = await res.json()
    if (!res.ok) throw new Error(body.error || 'Something went wrong')
    return body
  }

  const replaceSeat = (updated) =>
      setSeats((prev) => prev.map((s) => (s.id === updated.id ? updated : s)))

  async function handleSeatClick(seat) {
    setMessage('')
    try {
      const held = await post(`/api/seats/${seat.id}/hold`)
      replaceSeat(held)
      setSelected(held)
    } catch (e) {
      setMessage(e.message)
    }
  }

  async function handleConfirm() {
    try {
      const booked = await post(`/api/seats/${selected.id}/confirm`)
      replaceSeat(booked)
      setMessage(`Booked seat ${booked.rowLabel}${booked.seatNumber}!`)
    } catch (e) {
      setMessage(e.message)
    }
    setSelected(null)
  }

  // group seats by row
  const rows = {}
  seats.forEach((s) => {
    if (!rows[s.rowLabel]) rows[s.rowLabel] = []
    rows[s.rowLabel].push(s)
  })

  const secondsLeft = selected
      ? Math.max(0, Math.floor((new Date(selected.holdExpiresAt) - now) / 1000))
      : 0
  const mm = String(Math.floor(secondsLeft / 60)).padStart(2, '0')
  const ss = String(secondsLeft % 60).padStart(2, '0')

  return (
      <div className="page">
        <h1>Midnight Jazz Night</h1>
        <p className="sub">
          Blue Room Club ·{' '}
          <span className={connected ? 'live on' : 'live off'}>
          {connected ? '● Live' : '○ Offline'}
        </span>
        </p>

        <div className="stage">STAGE</div>

        <div className="grid">
          {Object.entries(rows).map(([label, rowSeats]) => (
              <div className="row" key={label}>
                <span className="row-label">{label}</span>
                {rowSeats.map((seat) => (
                    <button
                        key={seat.id}
                        className={`seat ${seat.status.toLowerCase()} ${
                            selected?.id === seat.id ? 'mine' : ''
                        }`}
                        disabled={seat.status !== 'AVAILABLE' || !!selected}
                        onClick={() => handleSeatClick(seat)}
                        title={`${seat.rowLabel}${seat.seatNumber} · price ${seat.price}`}
                    >
                      {seat.seatNumber}
                    </button>
                ))}
              </div>
          ))}
        </div>

        <div className="legend">
          <span><i className="dot available" /> Available</span>
          <span><i className="dot held" /> Held</span>
          <span><i className="dot booked" /> Booked</span>
        </div>

        {selected && (
            <div className="checkout">
              <p>
                Seat <b>{selected.rowLabel}{selected.seatNumber}</b> · price {selected.price}
              </p>
              <p>Hold expires in {mm}:{ss}</p>
              <button onClick={handleConfirm}>Pay (demo)</button>
            </div>
        )}

        {message && <p className="message">{message}</p>}
      </div>
  )
}