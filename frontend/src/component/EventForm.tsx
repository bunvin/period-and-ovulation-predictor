import './EventForm.css'

function EventForm(): JSX.Element {
    return (
        <div className="EventForm">
            <input type='date'></input><br />
            <button>Add period event</button>
            <h3>updated prediction</h3>
            <p>next period: "date" <br />
                next ovulation: "date"</p>
        </div>
    );
}

export default EventForm;