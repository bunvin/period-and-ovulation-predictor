import EventForm from './EventForm';
import EventList from './EventList';
import Header from './Header';
import './Layout.css';

function Layout(): JSX.Element {
    return (
        <div className='Layout'>
            <header>
                <Header />
            </header>
                <EventList />
            <div className="event-form">
                <EventForm />
            </div>
        </div>);
}

export default Layout;