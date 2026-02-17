import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Popover } from "bootstrap";

import RecordModal from "./RecordModal";
import TracksModal from "./TracksModal";


import { deleteRecord, getMyRecords } from "../api/recordService";
import { getTracksForRecord } from "../api/trackService";

export default function RecordList() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Record modal state
  const [showRecordModal, setShowRecordModal] = useState(false);
  const [selectedRecordForEdit, setSelectedRecordForEdit] = useState(null);

  // Tracks modal state
  const [showTracksModal, setShowTracksModal] = useState(false);
  const [selectedRecordForTracks, setSelectedRecordForTracks] = useState(null);

  // Tracks cache
  const [tracksByRecord, setTracksByRecord] = useState({});
  const [tracksLoading, setTracksLoading] = useState({});

  //track swipe
  const [swipe, setSwipe] = useState({}); // { [recordId]: { startX, x, dragging } }
  const SWIPE_THRESHOLD = 140;
  const SWIPE_DEADZONE = 0;     // must move at least this before we treat it as swipe
  const SWIPE_MAX = 220;

  const loadRecords = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getMyRecords();
      const data = res.data;
      setRecords(Array.isArray(data) ? data : []);
    } catch (e) {
      const status = e?.response?.status;
      setError(status ? `HTTP ${status}` : e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRecords();
  }, []);

  // Record modal handlers
  const openCreate = () => {
    setSelectedRecordForEdit(null);
    setShowRecordModal(true);
  };

  const openEdit = (record) => {
    setSelectedRecordForEdit(record);
    setShowRecordModal(true);
  };

  const closeRecordModal = (shouldRefresh) => {
    setShowRecordModal(false);
    setSelectedRecordForEdit(null);
    if (shouldRefresh) loadRecords();
  };

  // Delete record
  const handleDelete = async (id, { confirm = true } = {}) => {
  if (confirm && !window.confirm("Delete this record?")) return;

  try {
    await deleteRecord(id);
    loadRecords();
  } catch (e) {
    alert("Delete failed");
    console.error(e);
  }
};

  // Tracks modal handlers
  const openTracksModal = (record) => {
    setSelectedRecordForTracks(record);
    setShowTracksModal(true);
  };

const closeTracksModal = async (shouldRefresh = false, recordId = null) => {
  if (shouldRefresh && recordId) {
    await refreshTracksForRecord(recordId);
  }
  setShowTracksModal(false);
  setSelectedRecordForTracks(null);
};



  // Popover helpers
  const escapeHtml = (s) =>
    String(s)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");

  const buildPopoverHtml = (tracks) => {
    if (!tracks) return "Hover to load tracks...";
    if (tracks.length === 0) return "No tracks";

    return `
      <div style="max-width:260px">
        <ul style="margin:0; padding-left:16px;">
          ${tracks
            .slice(0, 10)
            .map((t) => `<li>${escapeHtml(t.title ?? "")}</li>`)
            .join("")}
        </ul>
        ${
          tracks.length > 10
            ? `<div class="text-muted" style="font-size:12px">+${
                tracks.length - 10
              } more</div>`
            : ""
        }
      </div>
    `;
  };

  const ensureTracksLoaded = async (recordId) => {
    if (tracksByRecord[recordId] || tracksLoading[recordId]) return;

    setTracksLoading((prev) => ({ ...prev, [recordId]: true }));
    try {
      const res = await getTracksForRecord(recordId);
      const list = Array.isArray(res.data) ? res.data : [];
      setTracksByRecord((prev) => ({ ...prev, [recordId]: list }));
    } catch {
      setTracksByRecord((prev) => ({ ...prev, [recordId]: [] }));
    } finally {
      setTracksLoading((prev) => ({ ...prev, [recordId]: false }));
    }
  };

const refreshTracksForRecord = async (recordId) => {
  setTracksLoading((p) => ({ ...p, [recordId]: true }));
  try {
    const res = await getTracksForRecord(recordId);
    const list = Array.isArray(res.data) ? res.data : [];
    setTracksByRecord((p) => ({ ...p, [recordId]: list }));
  } catch (e) {
    console.error(e);
    setTracksByRecord((p) => ({ ...p, [recordId]: [] }));
  } finally {
    setTracksLoading((p) => ({ ...p, [recordId]: false }));
  }
};

// Prevent swipe when interacting with buttons/inputs inside the row
const isInteractiveEl = (el) => {          
  if (!el) return false;
  return Boolean(el.closest("button, a, input, select, textarea, label"));
};


const onRowPointerDown = (recordId, e) => {
  if (isInteractiveEl(e.target)) return;

  e.currentTarget.setPointerCapture?.(e.pointerId);

  setSwipe((prev) => ({
    ...prev,
    [recordId]: { startX: e.clientX, x: 0, dragging: true },
  }));
};

const onRowPointerMove = (recordId, e) => {
  setSwipe((prev) => {
    const s = prev[recordId];
    if (!s?.dragging) return prev;

    const dx = e.clientX - s.startX;              // negative = left swipe
    const clamped = Math.min(0, Math.max(dx, -SWIPE_MAX)); // only left, capped
    return { ...prev, [recordId]: { ...s, x: clamped } };
  });
};

const onRowPointerUp = async (recordId) => {
  const x = swipe?.[recordId]?.x || 0;

  // reset UI immediately
  setSwipe((prev) => ({
    ...prev,
    [recordId]: { ...(prev[recordId] || {}), dragging: false, x: 0 },
  }));

  // delete only if dragged far enough left
  if (x <= -SWIPE_THRESHOLD) {
    await handleDelete(recordId, { confirm: false });
  }
};


  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="mb-0">Records</h2>

        <div className="d-flex gap-2">
          <Link to="/" className="btn btn-outline-secondary">
            Home
          </Link>

          <button className="btn btn-primary" onClick={openCreate}>
            Add Record
          </button>
        </div>
      </div>

      {loading && <div>Loading...</div>}
      {error && <div className="alert alert-danger">Error: {error}</div>}

      {!loading && !error && records.length === 0 && (
        <div className="alert alert-warning">No records found.</div>
      )}

      {!loading && !error && records.length > 0 && (
        <div className="table-responsive">
          <table className="table table-striped table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>Title</th>
                <th>Artist</th>
                <th>Label</th>
                <th>Genre</th>
                <th>Year</th>
                <th>Price</th>
                <th style={{ width: 280 }}>Actions</th>
              </tr>
            </thead>


          
            <tbody>
              {records.map((r) => {
                const rowX = swipe?.[r.id]?.x || 0;
                const isDragging = Boolean(swipe?.[r.id]?.dragging);

                return (
                  <tr
                    key={r.id}
                    onPointerDown={(e) => onRowPointerDown(r.id, e)}
                    onPointerMove={(e) => onRowPointerMove(r.id, e)}
                    onPointerUp={() => onRowPointerUp(r.id)}
                    onPointerCancel={() => onRowPointerUp(r.id)}
                    style={{
                      transform: `translateX(${rowX}px)`,
                      transition: isDragging ? "none" : "transform 160ms ease",
                      touchAction: "pan-y",
                      cursor: isDragging ? "grabbing" : "grab",
                      background: rowX <= SWIPE_THRESHOLD ? "#f8d7da" : undefined,
                    }}
                  >
                    <td>
                      {rowX < -40 && (
                        <span className="me-2 text-danger fw-semibold">Release to delete</span>
                      )}
                      {r.title}
                    </td>

                    <td>{r.artist}</td>
                    <td>{r.label ?? "-"}</td>
                    <td>{r.genre ?? "-"}</td>
                    <td>{r.publishingYear ?? "-"}</td>
                    <td>{r.price ?? "-"}</td>

                    <td>
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-outline-secondary btn-sm"
                          onClick={() => openEdit(r)}
                        >
                          Edit
                        </button>

                        <button
                          className="btn btn-outline-danger btn-sm"
                          onClick={() => handleDelete(r.id)}
                        >
                          Delete
                        </button>

                        <TracksButton
                          onClick={() => openTracksModal(r)}
                          onFirstHover={() => ensureTracksLoaded(r.id)}
                          getTracks={() => tracksByRecord[r.id]}
                          isLoading={() => Boolean(tracksLoading[r.id])}
                          buildPopoverHtml={buildPopoverHtml}
                        />
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>

          </table>
        </div>
      )}

      {/* Record create/edit modal */}
      <RecordModal
        show={showRecordModal}
        onClose={closeRecordModal}
        record={selectedRecordForEdit}
      />

      {/* Tracks modal */}
      <TracksModal
        show={showTracksModal}
        record={selectedRecordForTracks}
        onClose={closeTracksModal}
      />
    </>
  );
}

function TracksButton({
  onClick,
  onFirstHover,
  getTracks,
  isLoading,
  buildPopoverHtml,
}) {
  const btnRef = useRef(null);
  const popoverRef = useRef(null);

  useEffect(() => {
    const el = btnRef.current;
    if (!el) return;

    popoverRef.current = new Popover(el, {
      trigger: "hover focus",
      placement: "right",
      html: true,
      container: "body",
      title: "Tracks",
      content: () => (isLoading() ? "Loading..." : buildPopoverHtml(getTracks())),
    });

    return () => {
      popoverRef.current?.dispose();
      popoverRef.current = null;
    };
  }, [getTracks, isLoading, buildPopoverHtml]);

  const handleMouseEnter = async () => {
    await onFirstHover();

    const instance = popoverRef.current;
    if (instance) {
      instance.setContent({
        ".popover-header": "Tracks",
        ".popover-body": isLoading()
          ? "Loading..."
          : buildPopoverHtml(getTracks()),
      });
    }
  };

  return (
    <button
      ref={btnRef}
      type="button"
      className="btn btn-outline-primary btn-sm"
      onMouseEnter={handleMouseEnter}
      onClick={onClick}
    >
      Tracks
    </button>
  );
}
