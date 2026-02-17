import { useEffect, useState } from "react";
import { createTrack, getTracksForRecord  , deleteTrack} from "../api/trackService";

export default function TracksModal({ show, record, onClose }) {
  const [tracks, setTracks] = useState([]);
  const [form, setForm] = useState({ title: "", duration: "", key: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const recordId = record?.id;

  const [swipeTrack, setSwipeTrack] = useState({}); // { [trackId]: { startX, x, dragging } }
  const TRACK_SWIPE_THRESHOLD = 110;


  /// slide function added
  const isInteractiveEl = (el) => {
  if (!el) return false;
  return Boolean(el.closest("button, a, input, select, textarea, label"));
};

  const onTrackPointerDown = (trackId, e) => {
  if (isInteractiveEl(e.target)) return;

  e.currentTarget.setPointerCapture?.(e.pointerId);

  setSwipeTrack((prev) => ({
    ...prev,
    [trackId]: { startX: e.clientX, x: 0, dragging: true },
  }));
};

  const onTrackPointerMove = (trackId, e) => {
  setSwipeTrack((prev) => {
    const s = prev[trackId];
    if (!s?.dragging) return prev;

    const dx = e.clientX - s.startX;
    const x = Math.max(0, Math.min(dx, 180)); // right swipe only
    return { ...prev, [trackId]: { ...s, x } };
  });
};

  const onTrackPointerUp = async (trackId) => {
  const x = swipeTrack?.[trackId]?.x ?? 0;

  // Reset UI immediately
  setSwipeTrack((prev) => ({
    ...prev,
    [trackId]: { ...(prev[trackId] || {}), dragging: false, x: 0 },
  }));

  if (x < TRACK_SWIPE_THRESHOLD) return;

  const ok = window.confirm("Delete this track?");
  if (!ok) return;

  try {
    await deleteTrack(recordId, trackId); // IMPORTANT: your signature
    setTracks((prev) => prev.filter((t) => t.id !== trackId));
  } catch (e) {
    console.error(e);
    setError("Failed to delete track");
  }
};

const loadTracks = async () => {
  if (!recordId) return;

  setLoading(true);
  setError(null);
  try {
    const res = await getTracksForRecord(recordId);
    setTracks(Array.isArray(res.data) ? res.data : []);
  } catch (e) {
    console.error(e);
    setError("Failed to load tracks");
  } finally {
    setLoading(false);
  }
};



useEffect(() => {
  if (!show || !recordId) return;
  loadTracks();
}, [show, recordId]);

if (!show || !record) return null;



  const formatDuration = (seconds) => {
  if (seconds == null || Number.isNaN(seconds)) return "—";
  const s = Math.max(0, Number(seconds));
  const mm = String(Math.floor(s / 60)).padStart(2, "0");
  const ss = String(s % 60).padStart(2, "0");
  return `${mm}:${ss}`;
};

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

const handleSubmit = async (e) => {
  e.preventDefault();
  setError(null);

  const payload = {
    title: form.title,
    durationSeconds: parseInt(form.duration, 10),
    trackKey: form.key,
  };

  console.log("About to send payload:", payload);

  try {
    await createTrack(recordId, payload);

    setForm({ title: "", duration: "", key: "" });
    await loadTracks();

    const refreshed = await getTracksForRecord(recordId);
    setTracks(Array.isArray(refreshed.data) ? refreshed.data : []);
  } catch (e) {
    console.log("createTrack failed:", e?.response?.data || e);
    setError("Failed to create track");
  }
};


  return (
    <div className="modal show d-block" tabIndex="-1" style={{ zIndex: 1050 }}>
      <div className="modal-dialog modal-lg modal-dialog-scrollable">
        <div className="modal-content">

          {/* HEADER */}
          <div className="modal-header">
            <h5 className="modal-title">
              Tracks for: <strong>{record.title}</strong>
            </h5>
            <button
              type="button"
              className="btn-close"
              onClick={() => onClose(true , record.id)}
            />
          </div>

          {/* BODY */}
          <div className="modal-body">
            {error && <div className="alert alert-danger">{error}</div>}

            {/* TRACK LIST */}
            <ul className="list-group mb-4">
              {tracks.length === 0 && (
                <li className="list-group-item text-muted">
                  No tracks yet
                </li>
              )}



              {tracks.map((t) => {
                  const rowX = swipeTrack?.[t.id]?.x || 0;
                  const isDragging = Boolean(swipeTrack?.[t.id]?.dragging);

                  const title = t.title ?? "Untitled";
                  const durationSeconds = t.durationSeconds ?? t.duration ?? null;
                  const trackKey = t.trackKey ?? t.key ?? "—";

                  return (
                    <li
                      key={t.id}
                      className="list-group-item d-flex justify-content-between align-items-center"
                      onPointerDown={(e) => onTrackPointerDown(t.id, e)}
                      onPointerMove={(e) => onTrackPointerMove(t.id, e)}
                      onPointerUp={() => onTrackPointerUp(t.id)}
                      onPointerCancel={() => onTrackPointerUp(t.id)}
                      style={{
                        transform: `translateX(${rowX}px)`,
                        transition: isDragging ? "none" : "transform 160ms ease",
                        touchAction: "pan-y",
                        cursor: isDragging ? "grabbing" : "grab",
                        background: rowX >= TRACK_SWIPE_THRESHOLD ? "#f8d7da" : undefined,
                        cursor: isDragging ? "grabbing" : "grab",
                      }}
                    >
                      <div className="d-flex align-items-center gap-2">
                        {rowX < -40 && (
                          <span className="me-2 text-danger fw-semibold">
                            {rowX <= -SWIPE_THRESHOLD ? "Release to delete" : "Keep swiping"}
                          </span>
                        )}
                        <div className="fw-semibold">{title}</div>
                      </div>

                      <div className="d-flex gap-2 align-items-center">
                        <span className="badge text-bg-light">
                          Duration: {formatDuration(durationSeconds)}
                        </span>
                        <span className="badge text-bg-light">Key: {trackKey}</span>
                        <span className="text-muted">#{t.id}</span>
                      </div>
                    </li>
                  );
                })}


              
            </ul>

            {/* ADD TRACK FORM */}
            <form onSubmit={handleSubmit}>
              <div className="row g-2">
                <div className="col">
                  <input
                    className="form-control"
                    name="title"
                    placeholder="Title"
                    value={form.title}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="col-3">
                  <input
                    type="number"
                    className="form-control"
                    name="duration"
                    placeholder="Duration"
                    value={form.duration}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="col-3">
                  <input
                    className="form-control"
                    name="key"
                    placeholder="Key"
                    value={form.key}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="col-auto">
                  <button className="btn btn-primary" type="submit">
                    Add Track
                  </button>
                </div>
              </div>
            </form>
          </div>

          {/* FOOTER */}
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={() => onClose()}>
              Close
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}
