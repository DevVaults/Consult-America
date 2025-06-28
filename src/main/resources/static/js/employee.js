/**
 * Resume Management Application - Frontend JavaScript
 * Features:
 * - Resume upload with automatic tag extraction
 * - Display and manage resumes
 * - Download/delete functionality
 * - Toast notifications
 * - Loading indicators
 */

const API_BASE = "http://localhost:8080/employee/resumes";
let currentResumes = [];

// Document Ready Handler
$(document).ready(function () {
    initializeApplication();
});

// document.getElementById("filterByTags").addEventListener("change", function () {
//     const selectedTag = this.value.toLowerCase();
//     const rows = document.querySelectorAll("#resumeTableBody tr");
//     console.log(selectedTag);
//     console.log(rows);
//     rows.forEach(row => {
//         const tags = row.querySelector("td:nth-child(3)")?.innerText.toLowerCase() || "";
//         console.log(tags);
//         if (!selectedTag || tags.includes(selectedTag)) {
//             row.style.display = "";
//         } else {
//             row.style.display = "none";
//         }
//     });
// });


// document.getElementById("filterByTags").addEventListener("change", function () {
//     // Get selected values from Select2
//     const selectedOptions = $('#filterByTags').select2('data').map(item => item.text.toLowerCase());
//     const rows = document.querySelectorAll("#resumeTableBody tr");
//     console.log("rows",rows);
//     console.log("selectedOptions",selectedOptions);
//     rows.forEach(row => {
//         const tagsCell = row.querySelector("td:nth-child(3)");
//         console.log(tagsCell);
//         const tagsText = tagsCell?.innerText.toLowerCase() || "";
//         const rowTags = tagsText.split(',').map(tag => tag.trim());
        
//         // Check if all selected tags are present in the row's tags
//         const matches = selectedOptions.length === 0 || 
//                        selectedOptions.every(tag => rowTags.includes(tag));
        
//         row.style.display = matches ? "" : "none";
//     });
// });

$('#filterByTags').on('change', function() {
    const selectedOptions = $(this).val() || []; // Get selected values
    const rows = $("#resumeTableBody tr");

    console.log("selected Option", selectedOptions);
    console.log("rows", rows);

    rows.each(function() {
        const tagsText = $(this).find("td:nth-child(3)").text().toLowerCase() || "";
        const rowTags = tagsText.split(',').map(tag => tag.trim().toLowerCase());

        console.log("tagsText = ", tagsText);
        console.log("rowTags= ", rowTags);

        const matches = selectedOptions.length === 0 || 
                        selectedOptions.every(tag => rowTags.includes(tag.toLowerCase()));
        
        console.log("matches = ", matches);
        $(this).toggle(matches);
    });
});






function initializeApplication() {
    loadResumes();
    setupEventListeners();
    setupSearchFunctionality();
   // loadStats()
}

function setupEventListeners() {
    $("#uploadForm").on("submit", function (e) {
        e.preventDefault();
        handleResumeUpload();
    });

    $("#resumeFile").on("change", function () {
        previewFileName(this);
    });
}

function setupSearchFunctionality() {
    $("#searchInput").on("keyup", function () {
        const searchTerm = $(this).val().toLowerCase();
        filterResumes(searchTerm);
    });
}

function previewFileName(input) {
    const fileName = input.files[0]?.name || "No file selected";
    $("#filePreview").text(fileName).toggleClass("text-muted", !input.files[0]);
}

// ========================
// RESUME UPLOAD FUNCTIONS
// ========================

function handleResumeUpload() {
    if (!validateUploadForm()) return;

    const formData = new FormData();
    formData.append("file", $("#resumeFile")[0].files[0]);
    formData.append("name", $("#name").val().trim());
    formData.append("email", $("#email").val().trim());
    formData.append("contact", $("#contact").val().trim());
    formData.append("summary", $("#summary").val().trim());

    showLoading(true, "upload");

    $.ajax({
        url: API_BASE + "/upload",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            showTags(response.tags);
            loadResumes();
           // loadStats()
            showToast("Resume uploaded successfully!", "success");
            $("#uploadForm")[0].reset();
            $("#filePreview").text("No file selected").addClass("text-muted");
        },
        error: function (xhr) {
            const errorMsg =
                xhr.responseJSON?.message ||
                xhr.statusText ||
                "Upload failed. Please try again.";
            showToast(`Error: ${errorMsg}`, "error");
        },
        complete: function () {
            showLoading(false, "upload");
           // loadStats()
        },
    });
}

function validateUploadForm() {
    const name = $("#name").val().trim();
    const email = $("#email").val().trim();
    const contact = $("#contact").val().trim();
    const summary = $("#summary").val().trim();
    const file = $("#resumeFile")[0].files[0];

    if (!name) return showToast("Please enter candidate name", "warning"), false;
    if (!email) return showToast("Please enter email address", "warning"), false;
    if (!contact) return showToast("Please enter contact information", "warning"), false;
    if (!file) return showToast("Please select a resume file", "warning"), false;


    const validTypes = [
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ];
    if (!validTypes.includes(file.type)) {
        showToast("Only PDF and DOCX files are allowed", "warning");
        return false;
    }

    if (file.size > 5 * 1024 * 1024) {
        showToast("File size must be less than 5MB", "warning");
        return false;
    }

    return true;
}

function showTags(tags) {
    const $container = $("#tagsContainer").empty();
    $("#tagsCard").removeClass("d-none");

    if (!tags || tags.length === 0) {
        $container.append('<span class="badge bg-secondary">No tags found</span>');
        return;
    }

    tags.forEach((tag) => {
        $container.append(`<span class="badge bg-primary me-1 mb-1">${tag}</span>`);
    });
}

// ========================
// RESUME MANAGEMENT FUNCTIONS
// ========================

function loadResumes() {
    showLoading(true, "table");
console.log(API_BASE); // 👈 Logs the API base URL to inspect
    $.get(API_BASE + "/me")
    .done(function (resumes) {
        console.log("Received resumes response:", resumes); // 👈 Logs the response to inspect

        // Safely extract array in case response is wrapped in an object
        currentResumes = Array.isArray(resumes) ? resumes : [];


        console.log("currentResumes response:", currentResumes);
        renderResumes(currentResumes);
       // loadStats()
    })
    .fail(function () {
        showToast("Failed to load resumes. Please try again.", "error");
    })
    .always(function () {
        showLoading(false, "table");
    });

}

    async function sendProfileEmail(resumeId, recipientEmail,subject, message) {
    try {
        const response = await fetch(API_BASE +`/${resumeId}/send-profile`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                recipientEmail,
                subject: subject || '',
                customMessage: message || ''
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Email sent successfully!', 'success');
            return result;
        } else {
            throw new Error(result.error || 'Failed to send email');
        }
    } catch (error) {
        console.error('Email sending failed:', error);
        showToast('Failed to send email: ' + error.message, 'error');
        throw error;
    }
}
function showEmailDialog(resumeId) {
    Swal.fire({
        title: 'Send Profile via Email',
        html: `
           <div class="mb-3">
    <label class="form-label">Recipient Email <span class="text-danger">*</span></label>
    <input id="recipientEmail" class="form-control" type="email" placeholder="example@domain.com" required>
</div>

<div class="mb-3">
    <label class="form-label">Subject <span class="text-danger">*</span></label>
    <input id="emailSubject" class="form-control" type="text" placeholder="e.g., Profile Summary: John Doe" required>HANNAN TEST EMAIL
</div>

<div class="mb-3">
    <label class="form-label">Custom Message (optional)</label>
    <textarea id="customMessage" class="form-control" rows="3">test</textarea>
</div>

        `,
        focusConfirm: false,
        showCancelButton: true,
        confirmButtonText: 'Send Email',
        preConfirm: () => {
            const popup = Swal.getPopup();
            const email = popup.querySelector('#recipientEmail').value;
            const subject = popup.querySelector('#emailSubject').value;
            const message = popup.querySelector('#customMessage').value;

            if (!email) {
                Swal.showValidationMessage('Please enter recipient email');
                return false;
            }

            return { email, subject,message };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            sendProfileEmail(resumeId, result.value.email,result.value.subject, result.value.message);
        }
    });
}

//UI Integration
function setupEmailButton(resumeId) {
    const emailBtn = document.createElement('button');
    emailBtn.className = 'btn btn-sm btn-success ms-2';
    emailBtn.innerHTML = '<i class="bi bi-envelope"></i> Email Profile';
    emailBtn.onclick = () => showEmailDialog(resumeId);
    return emailBtn;
}
// this function is working now

function setupEmailButton(resumeId) {
    const btn = document.createElement('button');
    btn.className = 'btn btn-sm btn-success ms-2';
    btn.innerHTML = '<i class="bi bi-envelope"></i> Email';
    btn.onclick = () => showEmailDialog(resumeId);
    return btn;
}

function renderResumes(resumes) {
    const $tbody = $('#resumeTableBody').empty();
    
    if (resumes.length === 0) {
        $tbody.append(`
            <tr>
                <td colspan="5" class="text-center text-muted py-4">
                    No resumes found. Upload your first resume!
                </td>
            </tr>
        `);
        return;
    }
    
    resumes.forEach(resume => {
        console.log(resume);
        const date = resume.uploadedAt ? 
            new Date(resume.uploadedAt).toLocaleString() : 
            "N/A";
        console.log("Tags = ",resume.tags);
        const tags = resume.tags?.map(t => 
            `<span class="badge bg-secondary me-1 mb-1">${t}</span>`
        );

        // Create the table row
        const $row = $(`
            <tr data-resume-id="${resume.id}">
                <td>${resume.name || "N/A"}</td>
                <td>${resume.email || "N/A"}</td>
                <td class="tags-cell">${tags}</td>
                <td>${date}</td>
                <td class="actions-cell">
                    <div class="d-flex gap-2">
                        <button class="btn btn-sm btn-outline-primary download-btn">
                            <i class="bi bi-download"></i> Download
                        </button>
                        <button class="btn btn-sm btn-outline-danger delete-btn">
                            <i class="bi bi-trash"></i> Delete
                        </button>
                    </div>
                </td>
            </tr>
        `);
        
        // Get the actions cell div
        const actionsDiv = $row.find('.actions-cell div')[0];
        
        // Create and append the email button
        const emailBtn = setupEmailButton(resume.id);
        actionsDiv.appendChild(emailBtn);
        
        // Add event listeners
        $row.find('.download-btn').click(() => downloadResume(resume.id));
        $row.find('.delete-btn').click(() => confirmDeleteResume(resume.id));
        
        $tbody.append($row);
    });

}
function filterResumes(searchTerm) {
    if (!searchTerm) {
        renderResumes(currentResumes);
        return;
    }

    const filtered = currentResumes.filter((resume) => {
        return (
            (resume.name && resume.name.toLowerCase().includes(searchTerm)) ||
            (resume.email && resume.email.toLowerCase().includes(searchTerm)) ||
            (resume.tags &&
                resume.tags.some((tag) => tag.toLowerCase().includes(searchTerm)))
        );
    });

    renderResumes(filtered);
}

function downloadResume(id) {
    const resume = currentResumes.find((r) => r.id === id);
    if (!resume) {
        showToast("Resume not found", "error");
        return;
    }

    showLoading(true, "download");
    window.open(`${API_BASE}/${id}/download`, "_blank");
    setTimeout(() => showLoading(false, "download"), 1000);
}

function confirmDeleteResume(id) {
    const resume = currentResumes.find((r) => r.id === id);
    if (!resume) return;

    Swal.fire({
        title: "Delete Resume?",
        html: `Are you sure you want to delete <b>${resume.name}'s</b> resume?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#d33",
        cancelButtonColor: "#3085d6",
        confirmButtonText: "Yes, delete it!",
    }).then((result) => {
        if (result.isConfirmed) {
            deleteResume(id);
          //  loadStats()
        }
    });
}

function deleteResume(id) {
    showLoading(true, "delete");

    $.ajax({
        url: `${API_BASE}/${id}`,
        type: "DELETE",
        success: function () {
            showToast("Resume deleted successfully", "success");
            loadResumes();
            loadStats()
        },
        error: function () {
            showToast("Failed to delete resume", "error");
        },
        complete: function () {
            showLoading(false, "delete");
            
        },
    });
}

function showLoading(show, context = "") {
    switch (context) {
        case "upload":
            const $uploadBtn = $("#uploadForm button[type='submit']");
            $uploadBtn.prop("disabled", show);
            $uploadBtn.html(
                show
                    ? '<span class="spinner-border spinner-border-sm" role="status"></span> Uploading...'
                    : "Upload Resume"
            );
            break;

        case "table":
            const $tableBody = $("#resumeTableBody");
            if (show && $tableBody.children().length === 0) {
                $tableBody.html(`
                    <tr>
                        <td colspan="5" class="text-center py-4">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                        </td>
                    </tr>
                `);
            }
            break;

        case "delete":
        case "download":
            break;
    }
}

function showToast(message, type = "info") {
    const bgColor = {
        success: "#28a745",
        error: "#dc3545",
        warning: "#ffc107",
        info: "#17a2b8",
    }[type] || "#6c757d";

    Toastify({
        text: message,
        duration: 3000,
        close: true,
        gravity: "top",
        position: "right",
        backgroundColor: bgColor,
        stopOnFocus: true,
    }).showToast();







// Add this function to show the email dialog
function showEmailDialog(resumeId) {
    Swal.fire({
        title: 'Send Profile via Email',
        html: `
            <div class="mb-3">
                <label class="form-label">Recipient Email</label>
                <input id="recipientEmail" class="form-control" type="email" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Custom Message</label>
                <textarea id="customMessage" class="form-control" rows="3"></textarea>
            </div>
        `,
        confirmButtonText: 'Send Email',
        showCancelButton: true,
        preConfirm: () => {
            const email = document.getElementById('recipientEmail').value;
            const message = document.getElementById('customMessage').value;
            if (!email) {
                Swal.showValidationMessage('Please enter recipient email');
                return false;
            }
            return { email, message };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            sendProfileEmail(resumeId, result.value.email, result.value.message);
        }
    });
}

// Add this function to handle the email sending

// Then modify your renderResumes function to include the email button


// Function to create email button
function setupEmailButton(resumeId) {
    const btn = document.createElement('button');
    btn.className = 'btn btn-sm btn-success email-btn';
    btn.innerHTML = '<i class="bi bi-envelope"></i> Email';
    btn.onclick = (e) => {
        e.stopPropagation();
        showEmailDialog(resumeId);
    };
    return btn;
}

// Function to show email dialog
function showEmailDialog(resumeId) {
    Swal.fire({
        title: 'Send Resume & Summary via Email',
        html: `
            <div class="swal2-html-container">
                <div class="mb-2">
                    <label class="form-label">Recipient Email *</label>
                    <input type="email" id="swal-email" class="swal2-input" placeholder="email@example.com" required>
                ah1663227@gmail.com 
                    </div>
                <div class="mb-2">
                    <label class="form-label">Subject *</label>
                    <input type="text" id="swal-subject" class="swal2-input" placeholder="Email Subject" required>
                </div>
                <div class="mb-2">
                    <label class="form-label">Custom Message</label>
                    <textarea id="swal-message" class="swal2-textarea" rows="3" placeholder="Optional message...">test</textarea>
                </div>
            </div>
        `,
        focusConfirm: false,
        showCancelButton: true,
        confirmButtonText: 'Send Email',
        preConfirm: () => {
            const email = document.getElementById('swal-email').value.trim();
            const subject = document.getElementById('swal-subject').value.trim();
            const message = document.getElementById('swal-message').value.trim();

            if (!email || !subject) {
                Swal.showValidationMessage('Recipient email and subject are required');
                return false;
            }

            return { email, subject, message };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const { email, subject, message } = result.value;
            sendProfileEmail(resumeId, email, subject, message);
        }
    });
}


// Function to send email
async function sendProfileEmail(resumeId, email,subject, message) {
    try {
        showLoading(true);
        const response = await fetch(API_BASE+`${resumeId}/send-profile`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                recipientEmail: email,
                customMessage: message,
                subject:subject
            })
        });
        
        if (!response.ok) {
            throw new Error(await response.text());
        }
        
        showToast('Email sent successfully!', 'success');
    } catch (error) {
        console.error('Email error:', error);
        showToast(`Failed to send email: ${error.message}`, 'error');
    } finally {
        showLoading(false);
    }
}

}
