import * as React from 'react'

/** App has two components: number of likes and id number */
type LikeProps = {
    mId: number,
    mLikes: number
}

/** The Like component allows the user to interact with local state */
export class Like extends React.Component<LikeProps> {
    /**
     * The default state constructor for Like.
     */
    state = {
        mId: this.props.mId,
        mLikes: this.props.mLikes
    }
    /**
     * increment the number that is stored in the state
     * then POST to /messages/id/likes everytime the button is pressed
     */
    increment = (_e: React.MouseEvent<HTMLButtonElement>) => {
        // NB: setState will patch the state by updating any fields that are
        //     defined in the object that it is given.
        this.setState({ mLikes: ++this.state.mLikes });
        $.ajax({
            type: "POST",
            url: "/messages/" + this.props.mId + '/likes',
            //success: mainList.update
        });
    }

    decrement = (_e: React.MouseEvent<HTMLButtonElement>) => {
        
        this.setState({mLikes: --this.state.mLikes});
        $.ajax({
            type: "POST",
            url: "/messages/" + this.props.mId + "/likes",
        })
    }

    /**
     * Render the component.
     */
    render() {
        return (
            <span>
                <p>{this.state.mLikes}</p>
                <button onClick={this.increment}> Like</button>
                <button onClick={this.decrement}> Dislike</button>
            </span>
        );
    }
};