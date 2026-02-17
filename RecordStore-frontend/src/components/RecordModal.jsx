import { useEffect, useState } from "react";
import { createRecord, updateRecord } from "../api/recordService";

export default function RecordModal({ show, onClose, record }) {
  const [form, setForm] = useState({ title: "", artist: "", label: "" , genre: "" , publishingYear: "" , price: "" });
  const isEdit = Boolean(record);

  useEffect(() => {
    if (record) setForm(record);
  }, [record]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isEdit) await updateRecord(record.id, form);
    else await createRecord(form);
    onClose(true);
  };

  return (
  <>
    {/* Backdrop (behind the modal) */}
    {show && <div className="modal-backdrop fade show" style={{ zIndex: 1040 }} />}

    {/* Modal (above the backdrop) */}
    <div
      className={`modal fade ${show ? "show" : ""}`}
      style={{ display: show ? "block" : "none", zIndex: 1050 }}
      tabIndex="-1"
      role="dialog"
      aria-modal={show ? "true" : undefined}
      aria-hidden={show ? undefined : "true"}
    >
      <div className="modal-dialog" role="document">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{isEdit ? "Edit Record" : "Add Record"}</h5>
            <button
              type="button"
              className="btn-close"
              onClick={() => onClose(false)}
            />
          </div>

          <div className="modal-body">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Title</label>
                <input
                  name="title"
                  className="form-control"
                  value={form.title}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Artist</label>
                <input
                  name="artist"
                  className="form-control"
                  value={form.artist}
                  onChange={handleChange}
                  required
                />
              </div>

             <div className="mb-3">
                <label className="form-label">Label</label>
                <input
                  name="label"
                  className="form-control"
                  value={form.label}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Genre</label>
                <input
                  name="genre"
                  className="form-control"
                  value={form.genre}
                  onChange={handleChange}
                  required
                />
              </div>

               <div className="mb-3">
                <label className="form-label">Year</label>
                <input
                  type="number"
                  name="publishingYear"
                  className="form-control"
                  value={form.publishingYear}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Price</label>
                <input
                  type="number"
                  name="price"
                  className="form-control"
                  value={form.price}
                  onChange={handleChange}
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary">
                {isEdit ? "Update" : "Create"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </>
);
}
